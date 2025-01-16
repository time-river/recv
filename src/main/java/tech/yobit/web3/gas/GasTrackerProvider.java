package tech.yobit.web3.gas;

import org.jetbrains.annotations.NotNull;
import tech.yobit.web3.types.Address;
import tech.yobit.web3.types.GasTracker;

import java.math.BigInteger;

public interface GasTrackerProvider {
    boolean isSupported(long blockchainId);
    @NotNull
    GasTracker getGasTracker(long blockchainId);
    @NotNull
    BigInteger estimateGas(long blockchainId, @NotNull Address from, @NotNull Address to, @NotNull String data);
}
