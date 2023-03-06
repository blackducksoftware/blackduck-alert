package com.synopsys.integration.alert.api.distribution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJob;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.distribution.execution.JobStage;
import com.synopsys.integration.alert.api.distribution.mock.MockJobCompletionStatusDurationsRepository;
import com.synopsys.integration.alert.api.distribution.mock.MockJobCompletionStatusRepository;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.api.event.distribution.JobSubTaskEvent;
import com.synopsys.integration.alert.common.persistence.accessor.JobCompletionStatusModelAccessor;
import com.synopsys.integration.alert.database.api.DefaultJobCompletionStatusModelAccessor;
import com.synopsys.integration.alert.database.distribution.workflow.NotificationCorrelationToNotificationRelation;
import com.synopsys.integration.alert.database.distribution.workflow.NotificationCorrelationToNotificationRelationPK;
import com.synopsys.integration.alert.database.job.execution.JobCompletionDurationsRepository;
import com.synopsys.integration.alert.database.job.execution.JobCompletionRepository;

class JobSubTaskEventHandlerTest {
    private EventManager eventManager;
    private ExecutingJobManager executingJobManager;

    @BeforeEach
    public void init() {
        eventManager = Mockito.mock(EventManager.class);
        JobCompletionDurationsRepository jobCompletionDurationsRepository = new MockJobCompletionStatusDurationsRepository();
        JobCompletionRepository jobCompletionRepository = new MockJobCompletionStatusRepository(jobCompletionDurationsRepository);
        JobCompletionStatusModelAccessor jobCompletionStatusModelAccessor = new DefaultJobCompletionStatusModelAccessor(jobCompletionRepository, jobCompletionDurationsRepository);
        executingJobManager = new ExecutingJobManager(jobCompletionStatusModelAccessor);
    }

    private NotificationCorrelationToNotificationRelationPK getRelationKey(NotificationCorrelationToNotificationRelation relation) {
        return new NotificationCorrelationToNotificationRelationPK(relation.getNotificationCorrelationId(), relation.getNotificationId());
    }

    @Test
    void testHandleEvent() {
        String destination = "destination";
        UUID parentEventId = UUID.randomUUID();
        UUID jobExecutionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);

        ExecutingJob job = executingJobManager.startJob(jobId, 2);

        TestHandler handler = new TestHandler(eventManager, executingJobManager);
        TestEvent event = new TestEvent(destination, jobExecutionId, jobId, notificationIds);
        handler.handle(event);

        assertTrue(handler.wasHandlerCalled());
        Optional<ExecutingJob> entry = executingJobManager.getExecutingJob(job.getExecutionId());
        assertTrue(entry.isPresent());
        ExecutingJob savedJob = entry.get();
        assertEquals(job.getExecutionId(), savedJob.getExecutionId());
        assertEquals(jobId, savedJob.getJobConfigId());
        assertEquals(2, savedJob.getTotalNotificationCount());
        assertEquals(0, savedJob.getProcessedNotificationCount());
        assertEquals(0L, savedJob.getRemainingEvents());
        assertEquals(0L, savedJob.getExpectedNotificationsToSend());
        assertEquals(0L, savedJob.getNotificationsSent());
    }

    @Test
    void testHandleExceptionEvent() {

        String destination = "destination";
        UUID parentEventId = UUID.randomUUID();
        UUID jobExecutionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);

        TestHandler handler = new TestHandler(eventManager, executingJobManager);
        handler.setShouldThrowException(true);

        TestEvent event = new TestEvent(destination, jobExecutionId, jobId, notificationIds);
        handler.handle(event);

        assertTrue(handler.wasHandlerCalled());
    }

    @Test
    void testHandleEventCountToZero() {
        String destination = "destination";
        UUID parentEventId = UUID.randomUUID();
        UUID jobExecutionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);

        TestHandler handler = new TestHandler(eventManager, executingJobManager);
        TestEvent event = new TestEvent(destination, jobExecutionId, jobId, notificationIds);
        handler.handle(event);

        assertTrue(handler.wasHandlerCalled());
    }

    private static class TestEvent extends JobSubTaskEvent {
        private static final long serialVersionUID = -2052376174682165438L;

        public TestEvent(String destination, UUID jobExecutionId, UUID jobId, Set<Long> notificationIds) {
            super(destination, jobExecutionId, jobId, notificationIds);
        }
    }

    private static class TestHandler extends JobSubTaskEventHandler<TestEvent> {
        private boolean handlerCalled = false;
        private boolean shouldThrowException = false;

        protected TestHandler(
            EventManager eventManager,
            ExecutingJobManager executingJobManager
        ) {
            super(eventManager, JobStage.CHANNEL_PROCESSING, executingJobManager);

        }

        @Override
        protected void handleEvent(TestEvent event) throws AlertException {
            handlerCalled = true;
            if (shouldThrowException) {
                throw new AlertException("Test handler throws exception");
            }
        }

        public boolean wasHandlerCalled() {
            return handlerCalled;
        }

        public boolean shouldThrowException() {
            return shouldThrowException;
        }

        public void setShouldThrowException(boolean shouldThrowException) {
            this.shouldThrowException = shouldThrowException;
        }
    }
}
