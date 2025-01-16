package tech.yobit.web3.contract;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;
import tech.yobit.generated.wallet.Wallet;
import tech.yobit.web3.gas.GasProvider;
import tech.yobit.web3.types.*;
import tech.yobit.web3.utils.Constant;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 1. one blockchain has one wallet
 * 2. one wallet has multiple coins
 */
public class WalletContract {
    private static final Logger log = LoggerFactory.getLogger(WalletContract.class);

    private final Blockchain mBlockchain;
    private final ContractAddress mAddress;
    private final Credentials mCredentials;
    private final List<ERC20Meta> mCoinMetas = new ArrayList<>();

    private WalletContract(Address walletAddress, Blockchain blockchain,
                             Credentials credentials) {
        mBlockchain = blockchain;
        mAddress = new ContractAddress(blockchain.id, walletAddress);
        mCredentials = credentials;

        log.info("Wallet {} connected to {}({}) network",
                walletAddress.toHex(), blockchain.name, blockchain.id);
    }

    protected WalletContract(Address walletAddress, Blockchain blockchain,
                             ERC20Meta[] coinMetas, Credentials credentials)  {
        this(walletAddress, blockchain, credentials);

        for (ERC20Meta coinMeta : coinMetas) {
            updateCoinMeta(coinMeta);
        }
    }

    public ContractAddress getContractAddress() {
        return mAddress;
    }

    public void updateCoinMeta(ERC20Meta coinMeta) {
        if (coinMeta.contractAddress.blockchainId != mBlockchain.id) {
            log.warn("Coin contract address blockchain id {} mismatch, should be {}",
                    coinMeta.contractAddress.blockchainId, mBlockchain.id);
            return;
        }

        for (ERC20Meta mCoinMeta : mCoinMetas) {
            if (mCoinMeta.contractAddress.equals(coinMeta.contractAddress)) {
                log.warn("Coin {}({}) already exists", coinMeta.name, coinMeta.contractAddress.toHex());
                return;
            }
        }

        mCoinMetas.add(coinMeta);
    }

    public ERC20Meta[] getERC20MetaTypes() {
        return mCoinMetas.toArray(new ERC20Meta[0]);
    }

    public ERC20Meta findCoinMeta(Address address) {
        for (ERC20Meta val : mCoinMetas) {
            if (val.contractAddress.equals(address)) {
                return val;
            }
        }

        return null;
    }

    public BigInteger getCoinBalance(Address coinContractAddress) throws Exception {
        ERC20Meta coinMeta = findCoinMeta(coinContractAddress);
        if (coinMeta == null) {
            throw new Exception("Unknown coin contract address: " + coinContractAddress.toHex());
        }

        Function func = new Function(
                "balanceOf",
                Collections.singletonList(mAddress),
                List.of(new TypeReference<Uint256>() {
                })
        );
        String encodedFunc = FunctionEncoder.encode(func);

        Web3j web3j = Web3j.build(new HttpService(mBlockchain.rpcUrl));
        String rc = web3j.ethCall(
                Transaction.createEthCallTransaction(
                        Constant.ZERO_ADDRESS, coinContractAddress.toHex(), encodedFunc
                ),
                DefaultBlockParameterName.LATEST
        ).send().getValue();

        BigInteger amount = Numeric.toBigInt(rc);
        log.info("Wallet {}, {}({}) balance {} in {}({}))",
                mAddress.toHex(), coinMeta.name, coinMeta.contractAddress.toHex(),
                coinMeta.formatUnits(amount), mBlockchain.name, mBlockchain.id);

        return amount;
    }

    private String buildWithdrawTransactionData(Address to, BigInteger amount, Address token) {
        Function function = new Function(
                Wallet.FUNC_WITHDRAW,
                Arrays.<Type>asList(
                        new Address(to),
                        new Uint256(amount),
                        new Address(token)
                ),
                Collections.<TypeReference<?>>emptyList()
        );

        return FunctionEncoder.encode(function);
    }

    /**
     * the transaction will be received by blockchain, but **not ensure complete**
     */
    public String withdraw(Address to, Coin coin) throws Exception {
        Web3j web3j = Web3j.build(new HttpService(mBlockchain.rpcUrl));
        Wallet contract = Wallet.load(
                mAddress.toHex(),
                web3j, mCredentials,
                new GasProvider(mBlockchain.id, GasProvider.getEstimateGas(mBlockchain.id, buildWithdrawTransactionData(to, coin.value, coin.contractAddress)))
        );

        TransactionReceipt tx = contract.withdraw(
                to.toHex(), coin.parseUnits(), coin.contractAddress.toHex()
        ).send();

        String txId = tx.getTransactionHash();
        log.info("Wallet {} withdraw {}({}) to {}, txId {}, status {}, gas used {}",
                mAddress.toHex(), coin.formatUnits(), coin.parseUnits(), to.toHex(),
                txId, tx.isStatusOK(), tx.getGasUsed()
        );

        if (!tx.isStatusOK()) {
            log.error("Wallet {} withdraw failed, txId {}, msg:\n{}",
                    mAddress.toHex(), txId, tx.getLogs());
            return null;
        }

        return txId;
    }

    public String withdraw(Address to, BigInteger amount, Address coinContractAddress) throws Exception {
        ERC20Meta coinMeta = findCoinMeta(coinContractAddress);
        if (coinMeta == null) {
            throw new Exception("Unknown coin contract address: " + coinContractAddress.toHex());
        }

        Coin coin = new Coin(amount, coinMeta);
        return withdraw(to, coin);
    }

    public String withdraw(Address to, String amount, Address coinContractAddress) throws Exception {
        ERC20Meta coinMeta = findCoinMeta(coinContractAddress);
        if (coinMeta == null) {
            throw new Exception("Unknown coin contract address: " + coinContractAddress.toHex());
        }

        Coin coin = new Coin(amount, coinMeta);
        return withdraw(to, coin);
    }
}
