package com.synopsys.integration.alert.api.distribution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.distribution.execution.JobStage;
import com.synopsys.integration.alert.api.distribution.mock.MockJobSubTaskRepository;
import com.synopsys.integration.alert.api.distribution.mock.MockNotificationCorrelationToNotificationRelationRepository;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.api.event.distribution.JobSubTaskEvent;
import com.synopsys.integration.alert.common.persistence.accessor.JobSubTaskAccessor;
import com.synopsys.integration.alert.database.api.workflow.DefaultJobSubTaskAccessor;
import com.synopsys.integration.alert.database.distribution.workflow.JobSubTaskRepository;
import com.synopsys.integration.alert.database.distribution.workflow.JobSubTaskStatusEntity;
import com.synopsys.integration.alert.database.distribution.workflow.NotificationCorrelationToNotificationRelation;
import com.synopsys.integration.alert.database.distribution.workflow.NotificationCorrelationToNotificationRelationPK;
import com.synopsys.integration.alert.database.distribution.workflow.NotificationCorrelationToNotificationRelationRepository;

class JobSubTaskEventHandlerTest {
    private EventManager eventManager;
    private JobSubTaskAccessor jobSubTaskAccessor;
    private JobSubTaskRepository jobSubTaskRepository;
    private NotificationCorrelationToNotificationRelationRepository relationRepository;

    @BeforeEach
    public void init() {
        jobSubTaskRepository = new MockJobSubTaskRepository(JobSubTaskStatusEntity::getId);
        relationRepository = new MockNotificationCorrelationToNotificationRelationRepository(this::getRelationKey);
        eventManager = Mockito.mock(EventManager.class);
        jobSubTaskAccessor = new DefaultJobSubTaskAccessor(jobSubTaskRepository, relationRepository);
    }

    private NotificationCorrelationToNotificationRelationPK getRelationKey(NotificationCorrelationToNotificationRelation relation) {
        return new NotificationCorrelationToNotificationRelationPK(relation.getNotificationCorrelationId(), relation.getNotificationId());
    }

    @Test
    void testHandleEvent() {
        String destination = "destination";
        UUID jobExecutionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);

        jobSubTaskAccessor.createSubTaskStatus(jobExecutionId, jobId, 2L, notificationIds);

        TestHandler handler = new TestHandler(eventManager, jobSubTaskAccessor);
        TestEvent event = new TestEvent(destination, jobExecutionId, jobId, notificationIds);
        handler.handle(event);

        assertTrue(handler.wasHandlerCalled());
        Optional<JobSubTaskStatusEntity> entry = jobSubTaskRepository.findById(jobExecutionId);
        assertTrue(entry.isPresent());
        JobSubTaskStatusEntity entity = entry.get();
        assertEquals(jobExecutionId, entity.getId());
        assertEquals(jobId, entity.getJobId());
        assertEquals(1L, entity.getRemainingEvents());
        assertNotNull(entity.getNotificationCorrelationId());
        assertEquals(notificationIds.size(), relationRepository.count());
    }

    @Test
    void testHandleExceptionEvent() {

        String destination = "destination";
        UUID jobExecutionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);

        jobSubTaskAccessor.createSubTaskStatus(jobExecutionId, jobId, 2L, notificationIds);

        TestHandler handler = new TestHandler(eventManager, jobSubTaskAccessor);
        handler.setShouldThrowException(true);

        TestEvent event = new TestEvent(destination, jobExecutionId, jobId, notificationIds);
        handler.handle(event);

        assertTrue(handler.wasHandlerCalled());
        Optional<JobSubTaskStatusEntity> entry = jobSubTaskRepository.findById(jobExecutionId);
        assertTrue(entry.isEmpty());
    }

    @Test
    void testHandleEventCountToZero() {
        String destination = "destination";
        UUID jobExecutionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);

        jobSubTaskAccessor.createSubTaskStatus(jobExecutionId, jobId, 1L, notificationIds);

        TestHandler handler = new TestHandler(eventManager, jobSubTaskAccessor);
        TestEvent event = new TestEvent(destination, jobExecutionId, jobId, notificationIds);
        handler.handle(event);

        assertTrue(handler.wasHandlerCalled());
        Optional<JobSubTaskStatusEntity> entry = jobSubTaskRepository.findById(jobExecutionId);
        assertTrue(entry.isEmpty());
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
            JobSubTaskAccessor jobSubTaskAccessor
        ) {
            super(eventManager, jobSubTaskAccessor, JobStage.CHANNEL_PROCESSING);

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
