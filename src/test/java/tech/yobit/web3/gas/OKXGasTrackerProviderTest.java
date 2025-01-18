package tech.yobit.web3.gas;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Numeric;
import tech.yobit.generated.gateway.Gateway;
import tech.yobit.generated.wallet.Wallet;
import tech.yobit.web3.config.Configuration;
import tech.yobit.web3.config.GasTrackerConfig;
import tech.yobit.web3.config.OKXGasTrackerConfig;
import tech.yobit.web3.types.Address;
import tech.yobit.web3.types.BlockchainName;
import tech.yobit.web3.types.GasTracker;
import tech.yobit.web3.utils.Constant;
import tech.yobit.web3.utils.SetupTest;

import java.math.BigInteger;
import java.util.Arrays;
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
                break;
            }
        }

        Assertions.assertNotNull(config);
    }

    @Test
    public void testIsSupported() {
        OKXGasTrackerProvider provider = new OKXGasTrackerProvider(config);

        Assertions.assertTrue(provider.isSupported(BlockchainName.ETHEREUM));
        Assertions.assertTrue(provider.isSupported(BlockchainName.BNB_SMART_CHAIN));
        Assertions.assertTrue(provider.isSupported(BlockchainName.POLYGON));
        Assertions.assertTrue(provider.isSupported(BlockchainName.SEPOLIA));
        Assertions.assertFalse(provider.isSupported(BlockchainName.POLYGON_AMOY));
    }

    @Test
    public void testGetGasTrackerEthereum() {
        BlockchainName blockchainName = BlockchainName.ETHEREUM;
        OKXGasTrackerProvider provider = new OKXGasTrackerProvider(config);
        GasTracker tracker = provider.getGasTracker(blockchainName);
        logger.info("{}: {}", blockchainName, tracker);

        Assertions.assertNotNull(tracker);
        Assertions.assertNotEquals(new BigInteger("0"), tracker.safePriorityFee);
        Assertions.assertEquals(blockchainName.isSupportedEIP1559(), tracker.supportEIP1559);
    }

    @Test
    public void testGetGasTrackerBSC() {
        BlockchainName blockchainName = BlockchainName.BNB_SMART_CHAIN;
        OKXGasTrackerProvider provider = new OKXGasTrackerProvider(config);
        GasTracker tracker = provider.getGasTracker(blockchainName);
        logger.info("{}: {}", blockchainName, tracker);

        Assertions.assertNotNull(tracker);
        Assertions.assertNotEquals(DefaultGasProvider.GAS_PRICE, tracker.normalGasPrice);
        Assertions.assertEquals(blockchainName.isSupportedEIP1559(), tracker.supportEIP1559);
    }

    @Test
    public void testGetGasTrackerPolygon() {
        BlockchainName blockchainName = BlockchainName.POLYGON;
        OKXGasTrackerProvider provider = new OKXGasTrackerProvider(config);
        GasTracker tracker = provider.getGasTracker(blockchainName);
        logger.info("{}}: {}", blockchainName, tracker);

        Assertions.assertNotNull(tracker);
        Assertions.assertNotEquals(DefaultGasProvider.GAS_PRICE, tracker.normalGasPrice);
        Assertions.assertEquals(blockchainName.isSupportedEIP1559(), tracker.supportEIP1559);
    }

    @Test
    public void testGetGasTrackerSepolia() {
        BlockchainName blockchainName = BlockchainName.SEPOLIA;
        OKXGasTrackerProvider provider = new OKXGasTrackerProvider(config);
        GasTracker tracker = provider.getGasTracker(blockchainName);
        logger.info("{}: {}", blockchainName, tracker);

        Assertions.assertNotNull(tracker);
        Assertions.assertNotEquals(DefaultGasProvider.GAS_PRICE, tracker.normalGasPrice);
        Assertions.assertEquals(blockchainName.isSupportedEIP1559(), tracker.supportEIP1559);
    }

    private String buildCreateWalletTransactionData() {
        byte[] salt = Numeric.hexStringToByteArray(Constant.SHA256_BYTES);

        Function function = new Function(
                Gateway.FUNC_CREATEWALLET,
                List.of(new Bytes32(salt)),
                Collections.emptyList()
        );

        return FunctionEncoder.encode(function);
    }

    @Test
    public void testEstimateCreateWalletGas() {
        Configuration configs = SetupTest.buildConfig();
        Credentials credentials = Credentials.create(configs.privateKey);

        OKXGasTrackerProvider provider = new OKXGasTrackerProvider(config);
        BigInteger gasUsed = provider.estimateGas(
                BlockchainName.SEPOLIA,
                credentials.getAddress(),
                configs.blockchains[0].gatewayAddress,
                buildCreateWalletTransactionData()
        );

        logger.info("estimate CreateWallet gas: {}", gasUsed);
        Assertions.assertNotEquals(DefaultGasProvider.GAS_LIMIT, gasUsed);
    }

    private String buildWithdrawTransactionData(Address coinContractAddress, String amount) {
        Address address = Address.fromHex("");
        Function function = new Function(
                Wallet.FUNC_WITHDRAW,
                Arrays.<Type>asList(
                        address,
                        new Uint256(new BigInteger(amount)),
                        coinContractAddress
                ),
                Collections.<TypeReference<?>>emptyList()
        );

        return FunctionEncoder.encode(function);
    }

    @Test
    public void testEstimateWithdrawGas() {
        Configuration configs = SetupTest.buildConfig();
        Credentials credentials = Credentials.create(configs.privateKey);
        Address coinContractAddress = Address.fromHex(configs.coins[0].blockchains[0].coinContractAddress);

        OKXGasTrackerProvider provider = new OKXGasTrackerProvider(config);
        BigInteger gasUsed = provider.estimateGas(
                BlockchainName.SEPOLIA,
                credentials.getAddress(),
                SetupTest.SEPOLIA_WALLET_ADDRESS,
                buildWithdrawTransactionData(coinContractAddress, "1")
        );

        logger.info("estimate Withdraw gas: {}", gasUsed);
        Assertions.assertNotEquals(DefaultGasProvider.GAS_LIMIT, gasUsed);
    }
}
