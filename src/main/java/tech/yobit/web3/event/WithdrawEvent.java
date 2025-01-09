package tech.yobit.web3.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.yobit.web3.contract.WalletContract;
import tech.yobit.web3.types.Address;
import tech.yobit.web3.types.Coin;

import java.math.BigInteger;

/**
 * - monitor crypto coin balance
 * - withdraw it if the balance is enough
 */
public class WithdrawEvent implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(WithdrawEvent.class);

    private final WalletContract mWalletContract;
    private final Coin mCoin;
    private final Address mTo;
    private final Callback mCallback;
    private final int mTimeoutMs;

    // resolve, reject

    public WithdrawEvent(WalletContract walletContract,
                         Coin coin, Address to, Callback callback,
                         int timeoutMs) {
        if (coin.value.compareTo(BigInteger.ZERO) <= 0) {
            throw new IllegalArgumentException("coin value must be greater than zero");
        }

        mWalletContract = walletContract;
        mCoin = coin;
        mTo = to;
        mCallback = callback;
        mTimeoutMs = timeoutMs;
    }

    @Override
    public void run() {
        try {
            if (!mWalletContract.initialize()) {
                mCallback.reject(null);
            }

            String txId = mWalletContract.withdraw(mTo, mCoin);
            if (txId == null) {
                mCallback.reject(new Coin[]{mCoin});
            } else {
                mCallback.resolve(new Object[]{txId, mCoin});
            }
        } catch (Exception e) {
            log.error("withdraw Error", e);

            mCallback.reject(new Coin[]{mCoin});
        }
    }
}
