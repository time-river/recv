package tech.yobit.web3.types;

import java.math.BigInteger;

public class Coin extends CoinMeta {
    public final CoinMeta meta;
    public BigInteger value;

    public Coin(BigInteger value, CoinMeta meta)  {
        super(meta.name, meta.fullName, meta.decimals, meta.contractAddress.blockchainId, meta.contractAddress);
        this.value = value;
        this.meta = meta;
    }

    public Coin(String value, CoinMeta meta) {
        super(meta.name, meta.fullName, meta.decimals, meta.contractAddress.blockchainId, meta.contractAddress);
        this.value = meta.parseUnits(value);
        this.meta = meta;
    }

    public static Coin from(String value, CoinMeta meta) {
        BigInteger val = meta.parseUnits(value);
        return new Coin(val, meta);
    }

    public BigInteger parseUnits() {
       return this.value;
    }

    public String formatUnits() {
        return meta.formatUnits(this.value);
    }
}
