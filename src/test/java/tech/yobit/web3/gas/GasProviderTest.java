package tech.yobit.web3.gas;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.tx.gas.DefaultGasProvider;
import tech.yobit.generated.gateway.Gateway;
import tech.yobit.web3.config.GasTrackerConfig;
import tech.yobit.web3.utils.SetupTest;

public class GasProviderTest {
    private static final Logger logger = LoggerFactory.getLogger(GasProviderTest.class);

    @BeforeAll
    public static void init() {
        GasTrackerConfig[] configs = SetupTest.setupGasTrackerConfig();
        GasProvider.initialize(configs);
    }

    @Test
    public void testBlockchain1() {
        GasProvider gasProvider = new GasProvider(1);
        Assertions.assertEquals(1, gasProvider.getChainId());
        Assertions.assertNotEquals(DefaultGasProvider.GAS_PRICE, gasProvider.getGasPrice());
        logger.info("Ethernum Mainnet:\n\\- gasPrice: {}\n\\- isEIP1559Enabled: {}\n\\- maxFeePerGas: {}\n\\- maxPriorityFeePerGas: {}", gasProvider.getGasPrice(), gasProvider.isEIP1559Enabled(), gasProvider.getMaxFeePerGas(Gateway.FUNC_CREATEWALLET), gasProvider.getMaxPriorityFeePerGas(Gateway.FUNC_CREATEWALLET));
    }

}
