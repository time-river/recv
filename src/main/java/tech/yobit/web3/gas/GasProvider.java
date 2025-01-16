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
import tech.yobit.web3.types.Address;
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
    private static final LoadingCache<Long, GasTracker> mGasTrackers = Caffeine
            .newBuilder()
            .expireAfterWrite(15, TimeUnit.SECONDS)
            .build(blockchainId -> {
                for (GasTrackerProvider provider : mGasTrackersProviders) {
                    if (provider.isSupported(blockchainId)) {
                        return provider.getGasTracker(blockchainId);
                    }
                }

                return new DefaultGasTrackerProvider().getGasTracker(blockchainId);
            });

    private final long mBlockchainId;
    private final BigInteger mEstimateGas;
    static private boolean mInitialized;

    public static void initialize(GasTrackerConfig[] gasTrackerConfigs) {
        for (GasTrackerConfig config : gasTrackerConfigs) {
            String name = GasProvider.class.getPackageName() + "." + config.provider;

            try {
                Class<?> clazz = Class.forName(name);
                Class<?>[] parameterTypes = {GasTrackerConfig.class};
                java.lang.reflect.Constructor<?> constructor = clazz.getConstructor(parameterTypes);

                GasTrackerProvider instance = (GasTrackerProvider) constructor.newInstance(config);
                mGasTrackersProviders.add(instance);
            } catch (Exception e) {
                logger.warn("Failed to load gas tracker provider {}, class name {}", config.provider, name, e);
            }
        }

        mInitialized = true;
    }

    public static boolean isInitialized() {
        return mInitialized;
    }

    public GasProvider(long blockchainId) {
        mBlockchainId = blockchainId;
        mEstimateGas = DefaultGasProvider.GAS_LIMIT;
    }

    public GasProvider(long blockchainId, BigInteger estimateGas) {
        mBlockchainId = blockchainId;
        mEstimateGas = estimateGas;
    }

    static public BigInteger getEstimateGas(long blockchainId, @NotNull Address from, @NotNull Address to, @NotNull String data) {
        for (GasTrackerProvider provider : mGasTrackersProviders) {
            if (provider.isSupported(blockchainId)) {
                return provider.estimateGas(blockchainId, from, to, data);
            }
        }

        return DefaultGasProvider.GAS_LIMIT;
    }

    @Override
    public BigInteger getGasPrice(String contractFunc) {
        if (contractFunc.equals(Gateway.FUNC_CREATEWALLET)) {
            GasTracker tracker = mGasTrackers.get(mBlockchainId);
            // contract deploy maybe use high gas, therefore select low price
            return tracker.minGasPrice;
        } else if (contractFunc.equals(Wallet.FUNC_WITHDRAW)) {
            GasTracker tracker = mGasTrackers.get(mBlockchainId);
            return tracker.normalGasPrice;
        }
        return getGasPrice();
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
            // contract deploy maybe use high gas, therefore select low price
            return tracker.suggestBaseFee.add(tracker.proposePriorityFee);
        } else if (contractFunc.equals(Wallet.FUNC_WITHDRAW)) {
            return tracker.suggestBaseFee.add(tracker.safePriorityFee);
        } else {
            return tracker.suggestBaseFee.add(tracker.proposePriorityFee);
        }
    }

    @Override
    public BigInteger getMaxPriorityFeePerGas(String contractFunc) {
        GasTracker tracker = mGasTrackers.get(mBlockchainId);

        if (contractFunc.equals(Gateway.FUNC_CREATEWALLET)) {
            // contract deploy maybe use high gas, therefore select low price
            return tracker.proposePriorityFee;
        } else if (contractFunc.equals(Wallet.FUNC_WITHDRAW)) {
            return tracker.safePriorityFee;
        } else {
            return tracker.proposePriorityFee;
        }
    }
}
