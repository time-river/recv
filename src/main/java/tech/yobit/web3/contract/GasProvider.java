package tech.yobit.web3.contract;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractEIP1559GasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;

/**
 * TODO:
 *  1. price per gas selection own to different speed
 *  2. estimate gas limit
 */

public class GasProvider implements ContractEIP1559GasProvider {
    private static final Logger log = LoggerFactory.getLogger(GasProvider.class);

    private final long mBlockchainId;
    private final Web3j mWeb3j;
    private BigInteger mEstimateGas;

    public GasProvider(long blockchainId, String url, BigInteger estimateGas) {
        mBlockchainId = blockchainId;
        mWeb3j = Web3j.build(new HttpService(url));
        mEstimateGas = estimateGas;
    }

    public GasProvider(long blockchainId, String url) {
        mBlockchainId = blockchainId;
        mWeb3j = Web3j.build(new HttpService(url));
        mEstimateGas = DefaultGasProvider.GAS_LIMIT;
    }

    private BigInteger getGasPriceThroughNet() {
        BigInteger gasPrice = DefaultGasProvider.GAS_PRICE;

        try {
            gasPrice = mWeb3j.ethGasPrice().send().getGasPrice();
        } catch (Exception e) {
            log.warn("Failed to get gas price from network, use default gas price: {}, err msg: {}", gasPrice, e.getMessage());
        }

        return gasPrice;
    }

    @Override
    public BigInteger getGasPrice(String contractFunc) {
        return getGasPriceThroughNet();
    }

    @Override
    public BigInteger getGasPrice() {
        return getGasPriceThroughNet();
    }

    @Override
    public BigInteger getGasLimit(String contractFunc) {
        return mEstimateGas.multiply(BigInteger.valueOf(2));
    }

    @Override
    public BigInteger getGasLimit() {
        return mEstimateGas.multiply(BigInteger.valueOf(2));
    }

    public void setGasLimit(BigInteger gasLimit) {
        mEstimateGas = gasLimit;
    }

    @Override
    public boolean isEIP1559Enabled() {
        return true;
    }

    @Override
    public long getChainId() {
        return mBlockchainId;
    }

    @Override
    public BigInteger getMaxFeePerGas(String contractFunc) {
        return getGasPriceThroughNet();
    }

    @Override
    public BigInteger getMaxPriorityFeePerGas(String contractFunc) {
        return BigInteger.ZERO;
    }

}
