package tech.yobit.web3.gas;

import tech.yobit.web3.types.GasTracker;

import java.math.BigInteger;

public interface GasTrackerProvider {
    boolean isSupported(long blockchainId);
    GasTracker getGasTracker(long blockchainId);
    BigInteger estimateGas(long blockchainId, String data);
}
