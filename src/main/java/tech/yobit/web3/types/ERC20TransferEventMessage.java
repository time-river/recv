package tech.yobit.web3.types;

import java.math.BigInteger;

public class ERC20TransferEventMessage {
    public BigInteger blockNumber;
    public String transactionId;
    public Address from;
    public Address to;
    public Coin coin;

    public ERC20TransferEventMessage(BigInteger blockNumber, String transactionId, Address from, Address to, Coin coin) {
        this.blockNumber = blockNumber;
        this.transactionId = transactionId;
        this.from = from;
        this.to = to;
        this.coin = coin;
    }
}
