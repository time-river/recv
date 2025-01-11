package tech.yobit.web3.types;

import java.math.BigInteger;

public class Coin extends ERC20Meta {
    public BigInteger value;

    public Coin(BigInteger value, ERC20Meta meta) {
        super(meta.name, meta.fullName, meta.decimals, meta.contractAddress.blockchainId, meta.contractAddress);
        this.value = value;
    }

    public Coin(String value, ERC20Meta meta) {
        super(meta.name, meta.fullName, meta.decimals, meta.contractAddress.blockchainId, meta.contractAddress);
        this.value = parseUnits(value);
    }

    public static Coin from(String value, ERC20Meta meta) {
        return new Coin(value, meta);
    }

    public BigInteger parseUnits() {
        return this.value;
    }

    public String formatUnits() {
        return formatUnits(this.value);
    }
}
