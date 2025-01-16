package tech.yobit.web3.contract;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.yobit.web3.config.Configuration;
import tech.yobit.web3.gas.GasProvider;
import tech.yobit.web3.types.ContractAddress;
import tech.yobit.web3.utils.SetupTest;

import java.util.UUID;

public class GatewayContractTest {
    private static final Logger logger = LoggerFactory.getLogger(GatewayContractTest.class);

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
        Assertions.assertNotNull(contract);

        UUID uuid = UUID.randomUUID();
        ContractAddress address = contract.predictWalletAddress(uuid.toString());
        logger.info("predict address: {}", address);
        try {
            boolean exist = contract.checkWalletAddress(address);
            Assertions.assertFalse(exist);

            ContractAddress walletAddress = contract.createWallet(uuid.toString());
            logger.info("create address: {}", address);
            Assertions.assertEquals(address, walletAddress);
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }
}
