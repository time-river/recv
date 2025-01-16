package tech.yobit.web3.gas;

import org.web3j.tx.gas.DefaultGasProvider;
import tech.yobit.web3.types.GasTracker;

import java.math.BigInteger;

public class DefaultGasTrackerProvider implements GasTrackerProvider {

    @Override
    public boolean isSupported(long blockchainId) {
        return false;
    }

    @Override
    public GasTracker getGasTracker(long blockchainId)  {
        return new GasTracker();
    }

    @Override
    public BigInteger estimateGas(long blockchainId, String data) {
        return DefaultGasProvider.GAS_LIMIT;
    }
}
