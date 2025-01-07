package tech.yobit.web3.event;

import java.lang.Runnable;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventLoop {
    private static final Logger log = LoggerFactory.getLogger(EventLoop.class);

    private final LinkedBlockingDeque<Runnable> mEventQueue = new LinkedBlockingDeque<>();
    private final ExecutorService mExecutorService;

    public EventLoop(int workerCount) {
        mWorkerCount = workerCount;
        mExecutorService = Executors.newVirtualThreadPerTaskExecutor();
    }

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
            if (!mExecutorService.awaitTermination(2, TimeUnit.SECONDS)) {
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
