package tech.yobit.web3.types;

public class OKXGasLimitRequest {
    public String chainIndex;
    public String fromAddr;
    public String toAddr;
    public String txAmount;
    public ExtJSON extJson;

    static public class ExtJSON {
        public String inputData;
    }
}
