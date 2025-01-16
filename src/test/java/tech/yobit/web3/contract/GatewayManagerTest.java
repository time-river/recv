package tech.yobit.web3.contract;

import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.yobit.web3.config.Configuration;
import tech.yobit.web3.gas.GasProvider;
import tech.yobit.web3.types.Address;
import tech.yobit.web3.utils.SetupTest;

public class GatewayManagerTest {
    private static Logger logger = LoggerFactory.getLogger(GatewayManagerTest.class);

    private static Configuration config;

    @BeforeAll
    public static void init() {
        config = SetupTest.buildConfig();

        // Initialize gas provider
        GasProvider.initialize(config.gasTrackers);
    }

    @Test
    public void testConstructor() {
        try {
            new GatewayManager(
                config.privateKey,
                config.getBlockchainTypes(),
                config.getCoinMetaTypes()
            );
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void testGatewayContract() {
        GatewayManager manager;

        try {
            manager = new GatewayManager(
                config.privateKey,
                config.getBlockchainTypes(),
                config.getCoinMetaTypes()
            );

            Assertions.assertNull(manager.findGatewayContract(1));
            Assertions.assertNotNull(manager.findGatewayContract(11_155_111));


            Assertions.assertEquals(manager.getSupportedBlockchainTypes().length, config.getBlockchainTypes().length);

            Assertions.assertNull(manager.getWalletContract(1, null));
            Assertions.assertNotNull(manager.getWalletContract(11_155_111,
                    Address.fromString(config.coins[0].blockchains[0].coinContractAddress))
            );
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }
}
