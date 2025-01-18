package tech.yobit.web3.types;

import java.math.BigInteger;

public class ERC20TransferEventResult {
    public BigInteger blockNumber;
    public ERC20TransferEvent[] events = new ERC20TransferEvent[0];

    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("blockNumber: ").append(blockNumber.toString());
        for (tech.yobit.web3.types.ERC20TransferEvent event : events) {
            builder.append("\n\\-").append(event.toString());
        }

        return builder.toString();
    }
}
