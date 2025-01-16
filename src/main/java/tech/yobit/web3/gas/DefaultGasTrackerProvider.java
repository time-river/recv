package tech.yobit.web3.gas;

import org.jetbrains.annotations.NotNull;
import org.web3j.tx.gas.DefaultGasProvider;
import tech.yobit.web3.types.Address;
import tech.yobit.web3.types.GasTracker;

import java.math.BigInteger;

public class DefaultGasTrackerProvider implements GasTrackerProvider {

    @Override
    public boolean isSupported(long blockchainId) {
        return false;
    }

    @NotNull
    @Override
    public GasTracker getGasTracker(long blockchainId)  {
        return new GasTracker();
    }

    @NotNull
    @Override
    public BigInteger estimateGas(long blockchainId, @NotNull Address from, @NotNull Address to, @NotNull String data) {
        return DefaultGasProvider.GAS_LIMIT;
    }
}
