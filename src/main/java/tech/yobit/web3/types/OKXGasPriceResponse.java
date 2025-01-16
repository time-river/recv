package tech.yobit.web3.types;

import java.math.BigInteger;

/**
 * refer: <a href="https://www.okx.com/zh-hans/web3/build/docs/waas/walletapi-api-gas-price">OKX Gas Tracker</a>
 */
public class OKXGasPriceResponse extends OKXResponse {
    public DataItem[] data;

    static public class DataItem {
        public BigInteger normal;
        public BigInteger min;
        public BigInteger max;
        public Boolean supportEip1559;
        public ERC1559Protocol eip1559Protocol;
    }

    // TODO: use baseFee to adjust price
    static public class ERC1559Protocol {
        public BigInteger suggestBaseFee;
        public BigInteger baseFee;
        public BigInteger proposePriorityFee;
        public BigInteger safePriorityFee;
        public BigInteger fastPriorityFee;
    }
}
