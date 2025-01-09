package tech.yobit.web3.config;

// TODO
public class Configuration {
    public String privateKey;
    public String defaultUrl;
    public CoinConfig[] coins;
    public BlockchainConfig[] blockchains;

    public static Configuration parse() {
        return new Configuration();
    }
}
