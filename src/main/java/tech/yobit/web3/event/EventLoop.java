package tech.yobit.web3.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class EventLoop {
    private static final Logger log = LoggerFactory.getLogger(EventLoop.class);

    private static final LinkedBlockingDeque<Runnable> mEventQueue = new LinkedBlockingDeque<>();
    private static final ExecutorService mExecutorService = Executors.newVirtualThreadPerTaskExecutor();
    private static boolean stop = false;

    public static void postEvent(Runnable event) {
        mEventQueue.offer(event);
    }

    public static void start() {
        log.info("EventLoop start");
        stop = false;

        while (!stop) {
            try {
                Runnable event = mEventQueue.take();

                log.debug("EventLoop execute event");
                mExecutorService.execute(event);
                log.debug("EventLoop execute event end");
            } catch (InterruptedException e) {
                log.info("EventLoop is interrupted, shutdown");
                mExecutorService.shutdownNow();
                break;
            }
        }
    }

    public static Thread startAsync() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                start();
            }
        });
    }

    public static void shutdown() {
        log.info("EventLoop shutdown");
        stop = true;

        mExecutorService.shutdown();
        try {
            if (!mExecutorService.awaitTermination(60, TimeUnit.SECONDS)) {
                mExecutorService.shutdownNow();
                if (!mExecutorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.error("EventLoop did not terminate");
                }
            }
        } catch (InterruptedException e) {
            mExecutorService.shutdownNow();
        }
    }
}
