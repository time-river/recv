package tech.yobit.web3.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.yobit.web3.contract.WalletContract;
import tech.yobit.web3.types.Address;

import java.math.BigInteger;

/**
 * - try to withdraw coin
 * - retry in the future if failure
 */
public class WithdrawEvent implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(WithdrawEvent.class);

    private final WalletContract mWalletContract;
    private final Address mTo;
    private final Address mCoinAddress;
    private final Callback mCallback;

    public WithdrawEvent(WalletContract walletContract, Address to, Address coinAddress, Callback callback) {
        mWalletContract = walletContract;
        mTo = to;
        mCoinAddress = coinAddress;
        mCallback = callback;
    }

    @Override
    public void run() {
        Result rc = new Result(-1, mWalletContract, mTo, mCoinAddress);

        try {
            if (mWalletContract.initialize()) {
                BigInteger amount = mWalletContract.getCoinBalance(mCoinAddress);
                String txId = mWalletContract.withdraw(mTo, amount, mCoinAddress);

                if (txId != null) {
                    rc.status = 0;
                    rc.txId = txId;
                    rc.amount = amount;

                    mCallback.resolve(new Result[]{ rc });
                }

                rc.message = "No transaction id";
            }
        } catch (Exception e) {
            log.warn("withdraw Error", e);
            rc.message = String.format("withdraw error: %s", e.getMessage());
        }

        mCallback.reject(new Result[]{ rc });
    }

    static public class Result {
        public int status;
        public String message;
        public final WalletContract walletContract;
        public final Address to;
        public final Address coinAddress;
        public String txId;
        public BigInteger amount;

        public Result(int status, WalletContract walletContract, Address to, Address coinAddress) {
            this.status = status;
            this.walletContract = walletContract;
            this.to = to;
            this.coinAddress = coinAddress;
        }
    }
}
