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

    private static int WAIT_MS = 10 * 1000;
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

    private Coin isEligible() {
        long future = System.currentTimeMillis() + mTimeoutMs;
        Coin balance = new Coin("0", mCoin.meta);

        while (future >= System.currentTimeMillis()) {
            try {
                balance = mWalletContract.getCoinBalance(mCoin.meta.contractAddress);
                if (balance.value.compareTo(mCoin.value) >= 0) {
                    return balance;
                }

                Thread.sleep(WAIT_MS);
            } catch (Exception e) {
                log.error("getCoinBalance Error", e);
            }
        }

        return balance;
    }

    @Override
    public void run() {
        Coin balance = isEligible();
        if (balance.value.compareTo(mCoin.value)  < 0) {
            log.info("wallet {} withdraw {} {} isn't eligible, now balance: {}",
                    mWalletContract.getWalletAddress().toHex(),
                    mCoin.name, mCoin.formatUnits(),
                    balance.formatUnits()
            );

            mCallback.reject(new Coin[]{balance});
            return;
        }

        try {
            String txId = mWalletContract.withdraw(mTo, balance);
            if (txId == null) {
                mCallback.reject(new Coin[]{balance});
            } else {
                mCallback.resolve(new Object[]{txId, balance});
            }
        } catch (Exception e) {
            log.error("withdraw Error", e);

            mCallback.reject(new Coin[]{balance});
        }
    }
}
