package tech.yobit.web3.types;

import java.math.BigInteger;

public class Coin {
    public final CoinMeta meta;
    public BigInteger value;

    public Coin(BigInteger value, CoinMeta meta)  {
        this.value = value;
        this.meta = meta;
    }

    public static Coin from(BigInteger value, CoinMeta meta) {
        return new Coin(value, meta);
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
