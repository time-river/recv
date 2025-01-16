package tech.yobit.web3.contract;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.yobit.web3.config.CoinConfig;
import tech.yobit.web3.config.Configuration;
import tech.yobit.web3.config.GasTrackerConfig;
import tech.yobit.web3.config.OKXGasTrackerConfig;
import tech.yobit.web3.gas.GasProvider;
import tech.yobit.web3.utils.SetupTest;

public class GatewayContractTest {
    private static Logger logger = LoggerFactory.getLogger(GatewayContractTest.class);

    private static Configuration config;
    private static GatewayManager manager;

    @BeforeAll
    public static void init() {
        config = SetupTest.buildConfig();

        // Initialize gas provider
        GasProvider.initialize(config.gasTrackers);

        try {
            manager = new GatewayManager(
                    config.privateKey,
                    config.getBlockchainTypes(),
                    config.getCoinMetaTypes()
            );
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void testPredict() {
        GatewayContract contract = manager.findGatewayContract(11_155_111);
         //contract.predictWalletAddress("user");

         //contract.checkWalletAddress(null);
         //contract.createWallet("user");

//         contract.getWalletContract();
    }
}
