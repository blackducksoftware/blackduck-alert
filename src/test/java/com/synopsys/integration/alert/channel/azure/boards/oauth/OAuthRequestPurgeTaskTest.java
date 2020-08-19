package com.synopsys.integration.alert.channel.azure.boards.oauth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.scheduling.TaskScheduler;

import com.synopsys.integration.alert.common.workflow.task.TaskManager;

public class OAuthRequestPurgeTaskTest {
    @Test
    public void testCronSchedule() {
        OAuthRequestPurgeTask task = new OAuthRequestPurgeTask(null, null, null);
        assertEquals(OAuthRequestPurgeTask.CRON_EXPRESSION, task.scheduleCronExpression());
    }

    @Test
    public void testTaskRun() {
        TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        TaskManager taskManager = Mockito.mock(TaskManager.class);
        OAuthRequestValidator oAuthRequestValidator = new OAuthRequestValidator();
        String requestKey = "request-key-1";
        String requestKey2 = "request-key-2";
        oAuthRequestValidator.addAuthorizationRequest(requestKey);
        oAuthRequestValidator.addAuthorizationRequest(requestKey2);

        OAuthRequestPurgeTask task = new OAuthRequestPurgeTask(taskScheduler, taskManager, oAuthRequestValidator) {
            @Override
            protected Instant getRequestsBeforeInstant() {
                return Instant.now();
            }
        };
        task.runTask();
        assertFalse(oAuthRequestValidator.hasRequestKey(requestKey));
        assertFalse(oAuthRequestValidator.hasRequestKey(requestKey2));
    }
}
