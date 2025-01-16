package tech.yobit.web3.types;

import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;

public class GasTracker {
    public BigInteger normalGasPrice;
    public BigInteger minGasPrice;
    public BigInteger maxGasPrice;

    public boolean supportEIP1559;
    public BigInteger safePriorityFee;
    public BigInteger proposePriorityFee;
    public BigInteger fastPriorityFee;
    public BigInteger suggestBaseFee;

    public GasTracker() {
        this.normalGasPrice = DefaultGasProvider.GAS_PRICE;
        this.minGasPrice = DefaultGasProvider.GAS_PRICE;
        this.maxGasPrice = DefaultGasProvider.GAS_PRICE;

        this.supportEIP1559 = false;
        this.safePriorityFee = new BigInteger("0");
        this.proposePriorityFee = new BigInteger("0");
        this.fastPriorityFee = new BigInteger("0");
        this.suggestBaseFee = DefaultGasProvider.GAS_PRICE;
    }

    public String toString() {
        return "normalGasPrice=" + this.normalGasPrice
                + ", minGasPrice=" + this.minGasPrice
                + ", maxGasPrice=" + this.maxGasPrice
                + ", supportEIP1559=" + this.supportEIP1559
                + ", safePriorityFee=" + this.safePriorityFee
                + ", proposePriorityFee=" + this.proposePriorityFee
                + ", fastPriorityFee=" + this.fastPriorityFee
                + ", suggestBaseFee=" + this.suggestBaseFee;
    }

    public static GasTracker fromOKXGasTracker(OKXGasPriceResponse.DataItem data) {
        GasTracker gasTracker = new GasTracker();

        gasTracker.normalGasPrice = data.normal;
        gasTracker.minGasPrice = data.min;
        gasTracker.maxGasPrice = data.max;
        gasTracker.supportEIP1559 = data.supportEip1559;
        if (gasTracker.supportEIP1559) {
            gasTracker.safePriorityFee = data.eip1559Protocol.safePriorityFee;
            gasTracker.proposePriorityFee = data.eip1559Protocol.proposePriorityFee;
            gasTracker.fastPriorityFee = data.eip1559Protocol.fastPriorityFee;
            gasTracker.suggestBaseFee = data.eip1559Protocol.suggestBaseFee;
        }

        return gasTracker;
    }
}
