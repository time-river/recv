package tech.yobit.web3.config;

public class CoinConfig {
    public String name;
    public String fullName;
    public int decimals;
    public Blockchain[] blockchains;

    static public class Blockchain {
        public long id;
        public String coinContractAddress;
    }
}
