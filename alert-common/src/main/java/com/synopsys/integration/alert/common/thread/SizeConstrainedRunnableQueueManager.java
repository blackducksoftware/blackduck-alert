/**
 * alert-common
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
package com.synopsys.integration.alert.common.thread;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SizeConstrainedRunnableQueueManager<R extends Runnable> {
    private final int maxQueueSize;
    private final ExecutorService executorService;
    private final Queue<Future<?>> runnableQueue;
    private final AtomicInteger queuedElementsCount;

    public SizeConstrainedRunnableQueueManager(int maxQueueSize, ExecutorService executorService) {
        this.maxQueueSize = maxQueueSize;
        this.executorService = executorService;
        this.runnableQueue = new ConcurrentLinkedQueue<>();
        this.queuedElementsCount = new AtomicInteger(0);
    }

    public final void submit(R runnable) {
        Future<?> futureResult = executorService.submit(runnable);
        scheduleNewSyncTask(futureResult);
    }

    public final void removeCompletedTasks() {
        while (!runnableQueue.isEmpty() && runnableQueue.peek().isDone()) {
            runnableQueue.remove();
            queuedElementsCount.decrementAndGet();
        }
    }

    private void scheduleNewSyncTask(Future<?> syncTaskFuture) {
        if (queuedElementsCount.get() >= maxQueueSize) {
            Future<?> oldestTask = runnableQueue.poll();
            if (null != oldestTask) {
                oldestTask.cancel(true);
                queuedElementsCount.decrementAndGet();
            }
        }

        removeCompletedTasks();

        runnableQueue.add(syncTaskFuture);
        queuedElementsCount.incrementAndGet();
    }

}
