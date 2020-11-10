/**
 * provider
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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

        Runnable syncTask = new AddUserToProjectsRunnable(blackDuckProperties, blackDuckProjectNames);
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
