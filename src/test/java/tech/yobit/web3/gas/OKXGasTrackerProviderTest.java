package tech.yobit.web3.gas;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.tx.gas.DefaultGasProvider;
import tech.yobit.web3.config.GasTrackerConfig;
import tech.yobit.web3.config.OKXGasTrackerConfig;
import tech.yobit.web3.types.GasTracker;
import tech.yobit.web3.utils.SetupTest;

import java.math.BigInteger;

public class OKXGasTrackerProviderTest {
    private static final Logger logger = LoggerFactory.getLogger(OKXGasTrackerProviderTest.class);

    private static OKXGasTrackerConfig config;

    @BeforeAll
    public static void init() {
        GasTrackerConfig[] configs = SetupTest.setupGasTrackerConfig();
        for (GasTrackerConfig item : configs) {
            if (item.provider.equals("OKXGasTrackerProvider")) {
                config = (OKXGasTrackerConfig) item;
            }
            break;
        }

        Assertions.assertNotNull(config);
    }

    @Test
    public void testIsSupported() {
        OKXGasTrackerProvider provider = new OKXGasTrackerProvider(config);

        Assertions.assertTrue(provider.isSupported(1));
        Assertions.assertTrue(provider.isSupported(56));
        Assertions.assertTrue(provider.isSupported(137));
        Assertions.assertTrue(provider.isSupported(11155111));
        Assertions.assertFalse(provider.isSupported(2));
    }

    @Test
    public void testGetGasTracker1() {
        OKXGasTrackerProvider provider = new OKXGasTrackerProvider(config);
        GasTracker tracker = provider.getGasTracker(1);
        logger.info("1: {}", tracker.toString());
        Assertions.assertNotEquals(new BigInteger("0"), tracker.safePriorityFee);
    }

    @Test
    public void testGetGasTracker56() {
        OKXGasTrackerProvider provider = new OKXGasTrackerProvider(config);
        GasTracker tracker = provider.getGasTracker(56);
        logger.info("56: {}", tracker.toString());
        Assertions.assertNotEquals(DefaultGasProvider.GAS_PRICE, tracker.normalGasPrice);
    }

    @Test
    public void testGetGasTracker137() {
        OKXGasTrackerProvider provider = new OKXGasTrackerProvider(config);
        GasTracker tracker = provider.getGasTracker(137);
        logger.info("137: {}", tracker.toString());
        Assertions.assertNotEquals(DefaultGasProvider.GAS_PRICE, tracker.normalGasPrice);
    }

    @Test
    public void testGetGasTracker11155111() {
        OKXGasTrackerProvider provider = new OKXGasTrackerProvider(config);
        GasTracker tracker = provider.getGasTracker(11155111);
        logger.info("11_155_111: {}", tracker.toString());
        Assertions.assertNotEquals(DefaultGasProvider.GAS_PRICE, tracker.normalGasPrice);
    }
}
