package com.synopsys.integration.alert.provider.blackduck.action.delegator;

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
public class BlackDuckAddProjectUserDelegator {
    public static final int MAX_TASKS = 10;

    private final ExecutorService executorService;
    private final Queue<Future<?>> syncTasks;
    private final AtomicInteger taskCount;

    public BlackDuckAddProjectUserDelegator() {
        this.executorService = Executors.newCachedThreadPool();
        this.syncTasks = new ConcurrentLinkedQueue<>();
        this.taskCount = new AtomicInteger(0);
    }

    public void addProviderUserToBlackDuckProjects(BlackDuckProperties blackDuckProperties, Collection<String> blackDuckProjectHrefs) {
        if (blackDuckProjectHrefs.isEmpty()) {
            return;
        }

        Runnable syncTask = createTaskToSyncUserWithProjects(blackDuckProperties, blackDuckProjectHrefs);
        Future<?> syncTaskFuture = executorService.submit(syncTask);
        scheduleNewSyncTask(syncTaskFuture);
    }

    private void scheduleNewSyncTask(Future<?> syncTaskFuture) {
        if (taskCount.get() >= MAX_TASKS) {
            Future<?> longestRunningTask = syncTasks.poll();
            if (null != longestRunningTask) {
                longestRunningTask.cancel(true);
                taskCount.decrementAndGet();
            }
        }
        syncTasks.add(syncTaskFuture);
        taskCount.incrementAndGet();
    }

    private Runnable createTaskToSyncUserWithProjects(BlackDuckProperties blackDuckProperties, Collection<String> blackDuckProjectHrefs) {
        return () -> {
            // TODO implement
        };
    }

}
