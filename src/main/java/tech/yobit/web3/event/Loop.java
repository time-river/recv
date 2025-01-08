package tech.yobit.web3.event;

import java.lang.Runnable;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Loop {
    private static final Logger log = LoggerFactory.getLogger(Loop.class);

    private final LinkedBlockingDeque<Runnable> mEventQueue = new LinkedBlockingDeque<>();
    private final ExecutorService mExecutorService = Executors.newVirtualThreadPerTaskExecutor();

    public Loop() { }

    public void postEvent(Runnable event) {
        mEventQueue.offer(event);
    }

    public void start() {
        mExecutorService.execute(() -> {
            while (true) {
                try {
                    Runnable event = mEventQueue.take();
                    event.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    public void shutdown() {
        mExecutorService.shutdown();
        try {
            if (!mExecutorService.awaitTermination(3, TimeUnit.SECONDS)) {
                mExecutorService.shutdownNow();
                if (!mExecutorService.awaitTermination(3, TimeUnit.SECONDS)) {
                    log.error("EventLoop did not terminate");
                }
            }
        } catch (InterruptedException e) {
            mExecutorService.shutdownNow();
        }
    }
}
