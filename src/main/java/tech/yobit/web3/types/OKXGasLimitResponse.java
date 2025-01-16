package tech.yobit.web3.types;

public class OKXGasLimitResponse extends OKXResponse {
    public DataItem[] data;

    static public class DataItem {
        public String gasLimit;
    }
}
