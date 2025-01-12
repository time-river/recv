package tech.yobit.web3.config;

/**
 * @firstDepositAmount: 第一次充值最小金额
 * @minimumWithdrawalAmount: 提现最小金额
 */
public class Configuration {
    public String firstMinimumDepositAmount;
    public String minimumWithdrawalAmount;
    public String privateKey;
    public String defaultUrl;
    public CoinConfig[] coins;
    public BlockchainConfig[] blockchains;

    public static Configuration parse() {
        return new Configuration();
    }
}
