package com.synopsys.integration.alert.provider.blackduck.action.task;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;

@Component
public class BlackDuckProjectUserSyncTaskManager {
    public static final int MAX_TASKS = 10;

    private final ExecutorService executorService;
    private final Queue<Future<?>> syncTasks;
    private final AtomicInteger taskCount;

    public BlackDuckProjectUserSyncTaskManager() {
        this.executorService = Executors.newCachedThreadPool();
        this.syncTasks = new ConcurrentLinkedQueue<>();
        this.taskCount = new AtomicInteger(0);
    }

    public void addAlertGlobalBlackDuckUserToProjects(BlackDuckProperties blackDuckProperties, Collection<String> blackDuckProjectNames) {
        if (blackDuckProjectNames.isEmpty()) {
            return;
        }

        Runnable syncTask = new BlackDuckProjectSyncRunnable(blackDuckProperties, blackDuckProjectNames);
        Future<?> syncTaskFuture = executorService.submit(syncTask);
        scheduleNewSyncTask(syncTaskFuture);
    }

    public void removeCompletedTasks() {
        while (!syncTasks.isEmpty() && syncTasks.peek().isDone()) {
            syncTasks.remove();
            taskCount.decrementAndGet();
        }
    }

    private void scheduleNewSyncTask(Future<?> syncTaskFuture) {
        if (taskCount.get() >= MAX_TASKS) {
            Future<?> oldestTask = syncTasks.poll();
            if (null != oldestTask) {
                oldestTask.cancel(true);
                taskCount.decrementAndGet();
            }
        }

        removeCompletedTasks();

        syncTasks.add(syncTaskFuture);
        taskCount.incrementAndGet();
    }

}
