package tech.yobit.web3.gas;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.yobit.web3.types.BlockchainName;
import tech.yobit.web3.types.GasTracker;

import java.math.BigInteger;

public interface GasTrackerProvider {
    @NotNull
    String getName();

    boolean isSupported(BlockchainName blockchain);

    @Nullable
    GasTracker getGasTracker(BlockchainName blockchain);

    /**
     * @return:
     *     null: request failure, or provider don't supported
     *     BigInteger.ZERO: response emits error
     */
    @Nullable
    BigInteger estimateGas(BlockchainName blockchain, @NotNull String from, @NotNull String to, @NotNull String data);
}
