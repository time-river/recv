package tech.yobit.web3.gas;

import org.jetbrains.annotations.NotNull;
import org.web3j.tx.gas.DefaultGasProvider;
import tech.yobit.web3.types.BlockchainName;
import tech.yobit.web3.types.GasTracker;

import java.math.BigInteger;

public class DefaultGasTrackerProvider implements GasTrackerProvider {

    @NotNull
    @Override
    public String getName() {
        return DefaultGasProvider.class.getName();
    }

    @Override
    public boolean isSupported(BlockchainName blockchain) {
        return true;
    }

    @NotNull
    @Override
    public GasTracker getGasTracker(BlockchainName blockchain) {
        return new GasTracker(blockchain);
    }

    @NotNull
    @Override
    public BigInteger estimateGas(BlockchainName blockchain, @NotNull String from, @NotNull String to, @NotNull String data) {
        return DefaultGasProvider.GAS_LIMIT;
    }
}
