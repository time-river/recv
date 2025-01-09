package tech.yobit.web3.contract;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Numeric;

import tech.yobit.generated.wallet.Wallet;
import tech.yobit.web3.types.*;

/**
 * 1. one blockchain has one wallet
 * 2. one wallet has multiple coins
 */
public class WalletContract {
    private static final Logger log = LoggerFactory.getLogger(WalletContract.class);

    private final Web3j mWeb3j;
    private final String mUid;

    private final Blockchain mBlockchain;
    private final ContractAddress mAddress;
    private final GatewayContract mGatewayContract;
    private final Wallet mWalletContract;
    private final List<CoinMeta> mCoinMetas = new ArrayList<>();

    public WalletContract(Address walletAddress, Blockchain blockchain,
                          Credentials credentials, String uid, GatewayContract gatewayContract)   {
        mUid = uid;
        mGatewayContract = gatewayContract;
        mBlockchain = blockchain;
        mAddress = new ContractAddress(blockchain.id, walletAddress);

        mWeb3j = Web3j.build(new HttpService(blockchain.url));

        // TODO: fix gas limit
        mWalletContract = Wallet.load(
                walletAddress.toHex(),
                mWeb3j, credentials, new DefaultGasProvider()
        );

        log.info("Wallet {} connected to {}({}) network",
                walletAddress.toHex(), blockchain.name, blockchain.id);
    }

    public ContractAddress getWalletAddress() {
        return mAddress;
    }

    public boolean initialize() throws Exception {
        ContractAddress address = mGatewayContract.checkAndCreateWallet(mUid, mAddress);
        return address != null;
    }

    public boolean updateCoinMeta(CoinMeta coinMeta) {
        if (coinMeta.contractAddress.blockchainId != mBlockchain.id) {
            log.warn("Coin contract address blockchain id {} mismatch, should be {}",
                    coinMeta.contractAddress.blockchainId, mBlockchain.id);
            return false;
        }

        for (CoinMeta mCoinMeta : mCoinMetas) {
            if (mCoinMeta.contractAddress.equals(coinMeta.contractAddress)) {
                log.warn("Coin {}({}) already exists", coinMeta.name, coinMeta.contractAddress.toHex());
                return false;
            }
        }

        return mCoinMetas.add(coinMeta);
    }

    private CoinMeta findCoinMeta(Address address) {
        for (CoinMeta val : mCoinMetas) {
            if (val.contractAddress.equals(address)) {
                return val;
            }
        }

        return null;
    }

    public Coin getCoinBalance(Address coinContractAddress) throws Exception {
        CoinMeta coinMeta = findCoinMeta(coinContractAddress);
        if (coinMeta == null) {
            throw new Exception("Unknown coin contract address: " + coinContractAddress.toHex());
        }

        Function func = new Function(
                "balanceOf",
                Arrays.asList(mAddress),
                Arrays.asList(new TypeReference<Uint256>() {})
        );
        String encodedFunc = FunctionEncoder.encode(func);

        String rc = mWeb3j.ethCall(
                Transaction.createEthCallTransaction(
                        mAddress.toHex(), coinContractAddress.toHex(), encodedFunc
                ),
                DefaultBlockParameterName.LATEST
        ).send().getValue();

        Coin coin = new Coin(Numeric.toBigInt(rc), coinMeta);
        log.info("Wallet {}, {}({}) balance {} in {}({}))",
                mAddress.toHex(), coinMeta.name, coinMeta.contractAddress.toHex(),
                    coin.formatUnits(), mBlockchain.name, mBlockchain.id);

        return coin;
    }

    public String withdraw(Address to, Coin coin) throws Exception {
        TransactionReceipt tx = mWalletContract.withdraw(
                to.toHex(), coin.parseUnits(), coin.contractAddress.toHex()
        ).send();

        log.info("Wallet {} withdraw {}({}) to {}, txId {}, status {}, gas used {}",
                mAddress.toHex(), coin.formatUnits(), coin.parseUnits(), to.toHex(),
                tx.getTransactionHash(), tx.isStatusOK(), tx.getGasUsed()
        );

        if (tx.isStatusOK()) {
            return tx.getTransactionHash();
        } else {
            log.error("Wallet {} withdraw failed, txId {}, msg:\n{}",
                    mAddress.toHex(), tx.getTransactionHash(), tx.getLogs());

            return null;
        }
    }

    public String withdraw(Address to, BigInteger amount, Address coinContractAddress) throws Exception {
        CoinMeta coinMeta = findCoinMeta(coinContractAddress);
        if (coinMeta == null) {
            throw new Exception("Unknown coin contract address: " + coinContractAddress.toHex());
        }

        Coin coin = new Coin(amount, coinMeta);
        return withdraw(to, coin);
    }

    public String withdraw(Address to, String amount, Address coinContractAddress) throws Exception {
        CoinMeta coinMeta = findCoinMeta(coinContractAddress);
        if (coinMeta == null) {
            throw new Exception("Unknown coin contract address: " + coinContractAddress.toHex());
        }

        Coin coin = new Coin(amount, coinMeta);
        return withdraw(to, coin);
    }
}
