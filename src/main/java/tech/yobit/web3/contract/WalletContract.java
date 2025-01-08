package tech.yobit.web3.contract;

import java.math.BigInteger;
import java.util.Arrays;

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
import tech.yobit.web3.types.Address;
import tech.yobit.web3.types.BlockchainMeta;
import tech.yobit.web3.types.Coin;
import tech.yobit.web3.types.CoinMeta;

public class WalletContract {
    private static final Logger log = LoggerFactory.getLogger(WalletContract.class);

    private final Address mAddress;
    private final Wallet mWalletContract;

    public WalletContract(Address walletAddress, Credentials credentials, BlockchainMeta blockchain)  {
        Web3j web3j = Web3j.build(new HttpService(blockchain.url));

        mAddress = walletAddress;
        // TODO: fix gas limit
        mWalletContract = Wallet.load(
                walletAddress.toHex(),
                web3j, credentials, new DefaultGasProvider()
        );

        log.info("Wallet {} connected to {} network",
                walletAddress.toHex(), blockchain.name);
    }

    public Coin getCoinBalance(CoinMeta coinMeta) throws Exception {
        Function func = new Function(
                "balanceOf",
                Arrays.asList(mAddress),
                Arrays.asList(new TypeReference<Uint256>() {})
        );
        String encodedFunc = FunctionEncoder.encode(func);

        Web3j web3j = Web3j.build(new HttpService(coinMeta.blockchain.url));
        String rc = web3j.ethCall(
                Transaction.createEthCallTransaction(
                        mAddress.toHex(), coinMeta.coinContractAddress.toHex(), encodedFunc
                ),
                DefaultBlockParameterName.LATEST
        ).send().getValue();

        return Coin.from(Numeric.toBigInt(rc), coinMeta);
    }

    public String withdraw(Address to, BigInteger amount, Address coinContractAddress) throws Exception {
        TransactionReceipt tx = mWalletContract.withdraw(
                to.toHex(), amount, coinContractAddress.toHex()
        ).send();

        log.info("Wallet {} withdraw {} {} to {}, txId {}, status {}, gas used {}",
                mAddress.toHex(), amount, coinContractAddress.toHex(), to.toHex(),
                tx.getTransactionHash(), tx.isStatusOK(), tx.getGasUsed()
        );
        return tx.isStatusOK() ? tx.getTransactionHash() : null;
    }
}
