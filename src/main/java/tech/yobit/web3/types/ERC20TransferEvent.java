package tech.yobit.web3.types;

import java.math.BigInteger;

public class ERC20TransferEvent {
    public BigInteger blockNumber;
    public String transactionId;
    public Address from;
    public Address to;
    public Coin coin;

    public ERC20TransferEvent(BigInteger blockNumber, String transactionId, Address from, Address to, Coin coin) {
        this.blockNumber = blockNumber;
        this.transactionId = transactionId;
        this.from = from;
        this.to = to;
        this.coin = coin;
    }

    public String toString() {
        return String.format("blockNumber: %s, transactionId: %s, from: %s, to: %s, coin: %s %s",
                blockNumber, transactionId, from, to, coin.formatUnits(), coin.name);
    }
}
