package tech.yobit.web3.gas;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.crypto.Credentials;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Numeric;
import tech.yobit.generated.gateway.Gateway;
import tech.yobit.web3.config.Configuration;
import tech.yobit.web3.config.GasTrackerConfig;
import tech.yobit.web3.config.OKXGasTrackerConfig;
import tech.yobit.web3.types.Address;
import tech.yobit.web3.types.GasTracker;
import tech.yobit.web3.utils.SetupTest;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

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
        logger.info("1: {}", tracker);
        Assertions.assertNotEquals(new BigInteger("0"), tracker.safePriorityFee);
    }

    @Test
    public void testGetGasTracker56() {
        OKXGasTrackerProvider provider = new OKXGasTrackerProvider(config);
        GasTracker tracker = provider.getGasTracker(56);
        logger.info("56: {}", tracker);
        Assertions.assertNotEquals(DefaultGasProvider.GAS_PRICE, tracker.normalGasPrice);
    }

    @Test
    public void testGetGasTracker137() {
        OKXGasTrackerProvider provider = new OKXGasTrackerProvider(config);
        GasTracker tracker = provider.getGasTracker(137);
        logger.info("137: {}", tracker);
        Assertions.assertNotEquals(DefaultGasProvider.GAS_PRICE, tracker.normalGasPrice);
    }

    @Test
    public void testGetGasTracker11155111() {
        OKXGasTrackerProvider provider = new OKXGasTrackerProvider(config);
        GasTracker tracker = provider.getGasTracker(11155111);
        logger.info("11_155_111: {}", tracker);
        Assertions.assertNotEquals(DefaultGasProvider.GAS_PRICE, tracker.normalGasPrice);
    }

    public String buildCreateWalletTransactionData() {
        String saltHex = "34987c6020631532c4fa22a287cbdb3396170d70cd44a7fb00ff7b41b9195d89";
        byte[] salt = Numeric.hexStringToByteArray(saltHex);

        Function function = new Function(
                Gateway.FUNC_CREATEWALLET,
                List.of(new Bytes32(salt)),
                Collections.emptyList()
        );

        return FunctionEncoder.encode(function);
    }

    @Test
    public void testEstimateGas() {
        Configuration configs = SetupTest.buildConfig();
        Credentials credentials = Credentials.create(configs.privateKey);

        OKXGasTrackerProvider provider = new OKXGasTrackerProvider(config);
        BigInteger gasUsed = provider.estimateGas(
                11_155_111,
                Address.fromHex(credentials.getAddress()),
                Address.fromHex(configs.blockchains[0].gatewayAddress),
                buildCreateWalletTransactionData()
        );

        logger.info("estimate gas: {}", gasUsed);
        Assertions.assertNotEquals(DefaultGasProvider.GAS_LIMIT, gasUsed);
    }
}
