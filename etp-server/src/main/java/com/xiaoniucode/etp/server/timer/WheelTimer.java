package com.xiaoniucode.etp.server.timer;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 时间轮定时器，用于处理超时任务
 * @author xiaoniucode
 */
@Component
public class WheelTimer {
    private static final Logger logger = LoggerFactory.getLogger(WheelTimer.class);
    private final int wheelSize;
    /**
     * 每个 tick 的时长（ms）
     */
    private final long tickDurationMs;
    private final List<ConcurrentLinkedQueue<TimeoutTask>> wheel;
    private final AtomicInteger currentTick = new AtomicInteger(0);
    private final ScheduledExecutorService tickExecutor;

    public WheelTimer() {
        //360槽，1s tick，覆盖6分钟
        this(360, 1000);
    }

    public WheelTimer(int wheelSize, long tickDurationMs) {
        this.wheelSize = wheelSize;
        this.tickDurationMs = tickDurationMs;
        this.wheel = new ArrayList<>(wheelSize);
        for (int i = 0; i < wheelSize; i++) {
            wheel.add(new ConcurrentLinkedQueue<>());
        }
        this.tickExecutor = Executors.newSingleThreadScheduledExecutor();
        tickExecutor.scheduleAtFixedRate(this::advanceTick, tickDurationMs, tickDurationMs, TimeUnit.MILLISECONDS);
        logger.debug("WheelTimer 初始化：wheelSize={}, tickDurationMs={}", wheelSize, tickDurationMs);
    }

    /**
     * 调度一个新超时任务
     */
    public TimeoutHandle newTimeout(Runnable task, long delayMs) {
        long ticks = delayMs / tickDurationMs;
        if (ticks > wheelSize) {
            throw new IllegalArgumentException("延迟过长，需多级轮子支持");
        }
        int targetSlot = (currentTick.get() + (int) ticks) % wheelSize;
        TimeoutTask timeoutTask = new TimeoutTask(task);
        wheel.get(targetSlot).add(timeoutTask);
        return timeoutTask;
    }

    /**
     * 推进 tick，执行当前槽位任务
     */
    private void advanceTick() {
        int slot = currentTick.getAndIncrement() % wheelSize;
        ConcurrentLinkedQueue<TimeoutTask> tasks = wheel.get(slot);
        TimeoutTask task;
        while ((task = tasks.poll()) != null) {
            if (!task.isCancelled()) {
                try {
                    task.run();
                } catch (Exception e) {
                    logger.error("任务执行失败: {}", e.getMessage(), e);
                }
            }
        }
    }

    public void stop() {
        tickExecutor.shutdown();
        logger.debug("WheelTimer 已停止");
    }

    @PreDestroy
    public void destroy() {
        stop();
    }

    private static class TimeoutTask implements Runnable, TimeoutHandle {
        private final Runnable delegate;
        private final AtomicBoolean cancelled = new AtomicBoolean(false);

        TimeoutTask(Runnable delegate) {
            this.delegate = delegate;
        }

        @Override
        public void run() {
            if (!cancelled.get()) {
                delegate.run();
            }
        }

        @Override
        public boolean isCancelled() {
            return cancelled.get();
        }

        @Override
        public void cancel() {
            cancelled.set(true);
        }
    }

    public interface TimeoutHandle {
        void cancel();
        boolean isCancelled();
    }
}