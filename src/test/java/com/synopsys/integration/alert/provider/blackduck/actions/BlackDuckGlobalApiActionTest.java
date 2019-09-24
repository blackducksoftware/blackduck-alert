package com.synopsys.integration.alert.provider.blackduck.actions;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;

import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.workflow.task.ScheduledTask;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckValidator;
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckAccumulator;
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckProjectSyncTask;

public class BlackDuckGlobalApiActionTest {
    public static final Long CONSTANT_TIME = 1000L;
    private TaskManager taskManager = new TaskManager();

    @BeforeEach
    public void registerTask() {
        TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        ScheduledFuture future = Mockito.mock(ScheduledFuture.class);
        Mockito.when(taskScheduler.schedule(Mockito.any(), Mockito.any(Trigger.class))).thenReturn(future);
        Mockito.when(future.getDelay(TimeUnit.MILLISECONDS)).thenReturn(CONSTANT_TIME);
        taskManager.registerTask(new ScheduledTask(taskScheduler, BlackDuckAccumulator.TASK_NAME) {
            @Override
            public void runTask() {}
        });
        taskManager.registerTask(new ScheduledTask(taskScheduler, BlackDuckProjectSyncTask.TASK_NAME) {
            @Override
            public void runTask() {}
        });
    }

    @AfterEach
    public void unregisterTask() {
        taskManager.unregisterTask(BlackDuckAccumulator.TASK_NAME);
        taskManager.unregisterTask(BlackDuckProjectSyncTask.TASK_NAME);
    }

    @Test
    public void afterSaveActionSuccessTest() throws AlertException {
        BlackDuckProviderKey blackDuckProviderKey = new BlackDuckProviderKey();
        BlackDuckValidator blackDuckValidator = Mockito.mock(BlackDuckValidator.class);
        Mockito.when(blackDuckValidator.validate()).thenReturn(true);
        ProviderDataAccessor providerDataAccessor = Mockito.mock(ProviderDataAccessor.class);
        BlackDuckGlobalApiAction blackDuckGlobalApiAction = new BlackDuckGlobalApiAction(blackDuckProviderKey, blackDuckValidator, taskManager, providerDataAccessor);

        final Optional<String> initialAccumulatorNextRunTime = taskManager.getNextRunTime(BlackDuckAccumulator.TASK_NAME);
        final Optional<String> initialSyncNextRunTime = taskManager.getNextRunTime(BlackDuckProjectSyncTask.TASK_NAME);

        assertTrue(initialAccumulatorNextRunTime.isEmpty());
        assertTrue(initialSyncNextRunTime.isEmpty());

        blackDuckGlobalApiAction.afterSaveAction(null);
        final Optional<String> accumulatorNextRunTime = taskManager.getNextRunTime(BlackDuckAccumulator.TASK_NAME);
        final Optional<String> syncNextRunTime = taskManager.getNextRunTime(BlackDuckProjectSyncTask.TASK_NAME);

        assertTrue(accumulatorNextRunTime.isPresent());
        assertTrue(syncNextRunTime.isPresent());
    }

    @Test
    public void afterUpdateActionSuccessTest() throws AlertException {
        BlackDuckProviderKey blackDuckProviderKey = new BlackDuckProviderKey();
        BlackDuckValidator blackDuckValidator = Mockito.mock(BlackDuckValidator.class);
        Mockito.when(blackDuckValidator.validate()).thenReturn(true);
        ProviderDataAccessor providerDataAccessor = Mockito.mock(ProviderDataAccessor.class);
        BlackDuckGlobalApiAction blackDuckGlobalApiAction = new BlackDuckGlobalApiAction(blackDuckProviderKey, blackDuckValidator, taskManager, providerDataAccessor);

        final Optional<String> initialAccumulatorNextRunTime = taskManager.getNextRunTime(BlackDuckAccumulator.TASK_NAME);
        final Optional<String> initialSyncNextRunTime = taskManager.getNextRunTime(BlackDuckProjectSyncTask.TASK_NAME);

        assertTrue(initialAccumulatorNextRunTime.isEmpty());
        assertTrue(initialSyncNextRunTime.isEmpty());

        blackDuckGlobalApiAction.afterUpdateAction(null);
        final Optional<String> accumulatorNextRunTime = taskManager.getNextRunTime(BlackDuckAccumulator.TASK_NAME);
        final Optional<String> syncNextRunTime = taskManager.getNextRunTime(BlackDuckProjectSyncTask.TASK_NAME);

        assertTrue(accumulatorNextRunTime.isPresent());
        assertTrue(syncNextRunTime.isPresent());
    }
}
