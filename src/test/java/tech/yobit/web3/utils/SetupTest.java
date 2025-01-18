package tech.yobit.web3.utils;

import org.jetbrains.annotations.NotNull;
import tech.yobit.web3.config.*;
import tech.yobit.web3.config.InfuraGasTrackerConfig;

public class SetupTest {
    public static final String SEPOLIA_WALLET_ADDRESS = System.getenv("SEPOLIA_WALLET_ADDRESS");

    @NotNull
    public static Configuration buildConfig() {
        Configuration config = new Configuration();
        config.privateKey = System.getenv("PRIVATE_KEY");
        config.defaultRpcUrl = System.getenv("DEFAULT_RPCURL");
        config.coins = setupCoinConfig();
        config.blockchains = setupBlockchainConfig();
        config.gasTrackers = setupGasTrackerConfig();

        return config;
    }

    @NotNull
    public static CoinConfig[] setupCoinConfig() {
        CoinConfig sepoliaUSDT = new CoinConfig();
        sepoliaUSDT.name = "USDT";
        sepoliaUSDT.fullName = "Tether USD";
        sepoliaUSDT.decimals = 6;

        CoinConfig.Blockchain usdtBlockchain = new CoinConfig.Blockchain();
        usdtBlockchain.id = 11_155_111;
        usdtBlockchain.coinContractAddress = "0x7169D38820dfd117C3FA1f22a697dBA58d90BA06";

        sepoliaUSDT.blockchains = new CoinConfig.Blockchain[]{usdtBlockchain};


        CoinConfig polygonUSDC = new CoinConfig();
        polygonUSDC.name = "USDC";
        polygonUSDC.fullName = "USD Coin";
        polygonUSDC.decimals = 6;

        CoinConfig.Blockchain usdcBlockchain = new CoinConfig.Blockchain();
        usdcBlockchain.id = 80_002;

        polygonUSDC.blockchains = new CoinConfig.Blockchain[]{usdcBlockchain};

        // [0] - sepoliaUSDT
        return new CoinConfig[]{sepoliaUSDT};
    }

    @NotNull
    public static BlockchainConfig[] setupBlockchainConfig() {
        BlockchainConfig ethereum = new BlockchainConfig();
        ethereum.name = "Ethereum Sepolia";
        ethereum.id = 11_155_111;
        ethereum.rpcUrl = "https://sepolia.drpc.org";
        ethereum.blockExplorerUrl = "https://sepolia-explorer.drpc.org";
        ethereum.gatewayAddress = "0x43f7b162472ba1bb8967853db7fe9fba7589cbdc";

        BlockchainConfig polygon = new BlockchainConfig();
        polygon.name = "Polygon Amoy";
        polygon.id = 80_002;
        polygon.rpcUrl = "https://rpc-amoy.polygon.technology";
        polygon.blockExplorerUrl = "https://api-amoy.polygonscan.com/api";
        polygon.gatewayAddress = null;

        return new BlockchainConfig[]{ethereum};
    }

    @NotNull
    public static GasTrackerConfig[] setupGasTrackerConfig() {
        OKXGasTrackerConfig okxGasTrackerConfig = new OKXGasTrackerConfig();
        okxGasTrackerConfig.provider = "OKXGasTrackerProvider";
        okxGasTrackerConfig.project = System.getenv("OKX_PROJECT");
        okxGasTrackerConfig.apiKey = System.getenv("OKX_API_KEY");
        okxGasTrackerConfig.secretKey = System.getenv("OKX_SECRET_KEY");
        okxGasTrackerConfig.passphrase = System.getenv("OKX_PASSPHRASE");

        InfuraGasTrackerConfig infuraGasTrackerConfig = new InfuraGasTrackerConfig();
        infuraGasTrackerConfig.provider = "InfuraGasTrackerProvider";
        infuraGasTrackerConfig.apiKey = System.getenv("INFURA_API_KEY");
        infuraGasTrackerConfig.apiKeySecret = System.getenv("INFURA_API_KEY_SECRET");

        return new GasTrackerConfig[]{okxGasTrackerConfig, infuraGasTrackerConfig};
    }

}
