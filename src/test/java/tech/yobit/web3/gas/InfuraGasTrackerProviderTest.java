package tech.yobit.web3.gas;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.yobit.web3.config.GasTrackerConfig;
import tech.yobit.web3.types.BlockchainName;
import tech.yobit.web3.config.InfuraGasTrackerConfig;
import tech.yobit.web3.utils.SetupTest;

public class InfuraGasTrackerProviderTest {
    private static final Logger logger = LoggerFactory.getLogger(InfuraGasTrackerProviderTest.class);

    private static InfuraGasTrackerConfig config;
    private static InfuraGasTrackerProvider provider;

    @BeforeAll
    public static void init() {
        GasTrackerConfig[] configs = SetupTest.setupGasTrackerConfig();
        for (GasTrackerConfig item : configs) {
            if (item.provider.equals("InfuraGasTrackerProvider")) {
                config = (InfuraGasTrackerConfig) item;
                break;
            }
        }
    }

    @Test
    public void testGetTasTrackerEthereum() {
        InfuraGasTrackerProvider provider = new InfuraGasTrackerProvider(config);
        BlockchainName blockchain = BlockchainName.ETHEREUM;
        provider.getGasTracker(blockchain);
    }

    @Test
    public void testGetTasTrackerBSC() {
        InfuraGasTrackerProvider provider = new InfuraGasTrackerProvider(config);
        BlockchainName blockchain = BlockchainName.BNB_SMART_CHAIN;
        provider.getGasTracker(blockchain);
    }

    @Test
    public void testGetTasTrackerPolygon() {
        InfuraGasTrackerProvider provider = new InfuraGasTrackerProvider(config);
        BlockchainName blockchain = BlockchainName.POLYGON;
        provider.getGasTracker(blockchain);
    }

    @Test
    public void testGetTasTrackerPolygonAmoy() {
        InfuraGasTrackerProvider provider = new InfuraGasTrackerProvider(config);
        BlockchainName blockchain = BlockchainName.POLYGON_AMOY;
        provider.getGasTracker(blockchain);
    }

    @Test
    public void testGetTasTrackerSepolia() {
        InfuraGasTrackerProvider provider = new InfuraGasTrackerProvider(config);
        BlockchainName blockchain = BlockchainName.SEPOLIA;
        provider.getGasTracker(blockchain);
    }
}
