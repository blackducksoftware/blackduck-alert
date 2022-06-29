package com.synopsys.integration.alert.api.channel.issue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class IssueTrackerLockTest {

    @Test
    void testAcquireLockSuccess() {
        IssueTrackerChannelLock lock = new IssueTrackerChannelLock("test_lock");
        assertTrue(lock.getLock(10));
    }

    @Test
    void testAcquireLockWait() throws InterruptedException {
        IssueTrackerChannelLock lock = new IssueTrackerChannelLock("test_lock");
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        AtomicInteger acquisitions = new AtomicInteger(0);
        CompletionService<Void> completionService = new ExecutorCompletionService<>(executorService);
        completionService.submit(this.createRunnableThatSleeps(lock, 10, acquisitions, 500));
        completionService.submit(this.createRunnable(lock, 10, acquisitions));

        assertTrue(completionService.take().isDone());
        assertTrue(completionService.take().isDone());
        assertEquals(2, acquisitions.get());
    }

    @Test
    void testAcquireLockTimeout() throws InterruptedException {
        IssueTrackerChannelLock lock = new IssueTrackerChannelLock("test_lock");
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        AtomicInteger acquisitions = new AtomicInteger(0);
        CompletionService<Void> completionService = new ExecutorCompletionService<>(executorService);
        completionService.submit(this.createRunnableThatSleeps(lock, 1, acquisitions, 5000));
        completionService.submit(this.createRunnable(lock, 1, acquisitions));

        assertTrue(completionService.take().isDone());
        assertTrue(completionService.take().isDone());
        assertEquals(1, acquisitions.get());
    }

    @Test
    void testAcquireLockThreadInterrupted() throws InterruptedException {
        IssueTrackerChannelLock lock = new IssueTrackerChannelLock("test_lock");
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        AtomicInteger acquisitions = new AtomicInteger(0);
        CompletionService<Void> completionService = new ExecutorCompletionService<>(executorService);
        completionService.submit(() -> {
            Thread.sleep(100);
            Thread.currentThread().interrupt();
            throw new InterruptedException();
        });
        completionService.submit(this.createRunnableThatSleeps(lock, 10, acquisitions, 500));

        completionService.take();
        completionService.take();
        assertEquals(1, acquisitions.get());
    }

    private Callable<Void> createRunnableThatSleeps(IssueTrackerChannelLock lock, int timeoutInSeconds, AtomicInteger acquisitionCounter, int sleepMilliseconds) {
        return () -> {
            boolean acquired = lock.getLock(timeoutInSeconds);
            if (acquired) {
                acquisitionCounter.incrementAndGet();
            }
            try {
                Thread.sleep(sleepMilliseconds);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.release();
            }
            return null;
        };
    }

    private Callable<Void> createRunnable(IssueTrackerChannelLock lock, int timeoutInSeconds, AtomicInteger acquisitionCounter) {
        return () -> {
            boolean acquired = lock.getLock(timeoutInSeconds);
            if (acquired) {
                acquisitionCounter.incrementAndGet();
                lock.release();
            }
            return null;
        };
    }

}
