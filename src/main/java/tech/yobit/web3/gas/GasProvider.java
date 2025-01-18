package tech.yobit.web3.gas;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.tx.gas.ContractEIP1559GasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import tech.yobit.generated.gateway.Gateway;
import tech.yobit.generated.wallet.Wallet;
import tech.yobit.web3.config.GasTrackerConfig;
import tech.yobit.web3.types.BlockchainName;
import tech.yobit.web3.types.GasTracker;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Design:
 * 1. one GasProvider Instance one request
 * 2. refresh gas price and gas usage per 15 seconds
 */

public class GasProvider implements ContractEIP1559GasProvider {
    private static final Logger logger = LoggerFactory.getLogger(GasProvider.class);

    private static final List<GasTrackerProvider> mGasTrackersProviders = new ArrayList<>();
    private static final LoadingCache<Long, GasTracker> mGasTrackers = Caffeine.newBuilder()
            .expireAfterWrite(15, TimeUnit.SECONDS)
            .build(blockchainId -> {
                BlockchainName blockchain = BlockchainName.from(blockchainId);
                for (GasTrackerProvider provider : mGasTrackersProviders) {
                    if (provider.isSupported(blockchain)) {
                        GasTracker tracker = provider.getGasTracker(blockchain);
                        // retry others when failure
                        if (tracker != null) {
                            return tracker;
                        }
                        logger.info("{} getGasTracker failure, try next", provider.getName());
                    }
                }

                logger.warn("Failed to get gas tracker for blockchain id {}, use default gas tracker provider", blockchainId);
                return new DefaultGasTrackerProvider().getGasTracker(blockchain);
            });
    static private boolean mInitialized;
    private final long mBlockchainId;
    private final BigInteger mEstimateGas;

    public GasProvider(long blockchainId) {
        mBlockchainId = blockchainId;
        mEstimateGas = DefaultGasProvider.GAS_LIMIT;
    }

    public GasProvider(long blockchainId, BigInteger estimateGas) {
        mBlockchainId = blockchainId;
        mEstimateGas = estimateGas;
    }

    public static void initialize(GasTrackerConfig[] gasTrackerConfigs) {
        for (GasTrackerConfig config : gasTrackerConfigs) {
            String name = GasProvider.class.getPackageName() + "." + config.provider;

            try {
                Class<?> clazz = Class.forName(name);
                Class<?>[] parameterTypes = {GasTrackerConfig.class};
                java.lang.reflect.Constructor<?> constructor = clazz.getConstructor(parameterTypes);

                GasTrackerProvider instance = (GasTrackerProvider) constructor.newInstance(config);
                mGasTrackersProviders.add(instance);
                logger.info("initialize {}", instance.getName());
            } catch (Exception e) {
                logger.warn("Failed to load gas tracker provider {}, class name {}", config.provider, name, e);
            }
        }

        mInitialized = true;
    }

    public static boolean isInitialized() {
        return mInitialized;
    }

    @NotNull
    static public BigInteger getEstimateGas(BlockchainName blockchain, @NotNull String from, @NotNull String to, @NotNull String data) {
        BigInteger amount = null;

        // TODO: optimize
        for (GasTrackerProvider provider : mGasTrackersProviders) {
            if (provider.isSupported(blockchain)) {
                amount = provider.estimateGas(blockchain, from, to, data);
                // retry others when failure
                if (amount == null) {
                    logger.info("{} getEstimateGas failure, try next", provider.getName());
                } else if (amount.equals(BigInteger.ZERO)) {
                    return BigInteger.ZERO;
                } else {
                    return amount;
                }
            }
        }

        logger.warn("Failed to get estimate gas for blockchain {}({}), use default gas", blockchain, blockchain.getId());
        return new DefaultGasTrackerProvider().estimateGas(blockchain, from, to, data);
    }

    @Override
    public BigInteger getGasPrice(String contractFunc) {
        GasTracker tracker = mGasTrackers.get(mBlockchainId);
        if (contractFunc.equals(Gateway.FUNC_CREATEWALLET)) {
            return tracker.normalGasPrice;
        } else if (contractFunc.equals(Wallet.FUNC_WITHDRAW)) {
            return tracker.normalGasPrice;
        } else {
            return tracker.normalGasPrice;
        }
    }

    @Override
    public BigInteger getGasPrice() {
        GasTracker tracker = mGasTrackers.get(mBlockchainId);
        return tracker.normalGasPrice;
    }

    @Override
    public BigInteger getGasLimit(String contractFunc) {
        return mEstimateGas;
    }

    @Override
    public BigInteger getGasLimit() {
        return mEstimateGas;
    }

    @Override
    public boolean isEIP1559Enabled() {
        GasTracker tracker = mGasTrackers.get(mBlockchainId);
        return tracker.supportEIP1559;
    }

    @Override
    public long getChainId() {
        return mBlockchainId;
    }

    @Override
    public BigInteger getMaxFeePerGas(String contractFunc) {
        GasTracker tracker = mGasTrackers.get(mBlockchainId);

        if (contractFunc.equals(Gateway.FUNC_CREATEWALLET)) {
            // contract deploy need speed
            return tracker.suggestBaseFee.add(tracker.proposePriorityFee);
        } else if (contractFunc.equals(Wallet.FUNC_WITHDRAW)) {
            return tracker.suggestBaseFee.add(tracker.safePriorityFee);
        } else {
            return tracker.suggestBaseFee.add(tracker.safePriorityFee);
        }
    }

    @Override
    public BigInteger getMaxPriorityFeePerGas(String contractFunc) {
        GasTracker tracker = mGasTrackers.get(mBlockchainId);

        if (contractFunc.equals(Gateway.FUNC_CREATEWALLET)) {
            // contract deploy need speed
            return tracker.proposePriorityFee;
        } else if (contractFunc.equals(Wallet.FUNC_WITHDRAW)) {
            return tracker.safePriorityFee;
        } else {
            return tracker.safePriorityFee;
        }
    }
}
