package com.synopsys.integration.alert.database.api.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.persistence.model.job.workflow.JobSubTaskStatusModel;

class DefaultJobSubTaskAccessorTest {

    private DefaultJobSubTaskAccessor accessor;

    @BeforeEach
    public void init() {
        MockJobSubTaskStatusRepository subTaskRepository = new MockJobSubTaskStatusRepository();
        MockCorrelationToNotificationRelationRepository relationRepository = new MockCorrelationToNotificationRelationRepository();

        accessor = new DefaultJobSubTaskAccessor(subTaskRepository, relationRepository);
    }

    @Test
    void testCreateSubTask() {
        UUID id = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Long remainingTaskCount = 0L;
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);
        JobSubTaskStatusModel model = accessor.createSubTaskStatus(id, jobId, remainingTaskCount, notificationIds);
        assertNotNull(model);
        assertEquals(id, model.getParentEventId());
        assertEquals(jobId, model.getJobId());
        assertEquals(remainingTaskCount, model.getRemainingTaskCount());
        assertNotNull(model.getNotificationCorrelationId());
    }

    @Test
    void testGetSubTaskEmpty() {
        UUID id = UUID.randomUUID();
        Optional<JobSubTaskStatusModel> model = accessor.getSubTaskStatus(id);
        assertTrue(model.isEmpty());
    }

    @Test
    void testGetSubTask() {
        UUID id = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Long remainingTaskCount = 0L;
        Set<Long> notificationIds = Set.of();
        JobSubTaskStatusModel createdModel = accessor.createSubTaskStatus(id, jobId, remainingTaskCount, notificationIds);
        Optional<JobSubTaskStatusModel> savedModel = accessor.getSubTaskStatus(id);
        assertTrue(savedModel.isPresent());
        JobSubTaskStatusModel model = savedModel.get();
        assertEquals(createdModel.getParentEventId(), model.getParentEventId());
        assertEquals(createdModel.getJobId(), model.getJobId());
        assertEquals(createdModel.getRemainingTaskCount(), model.getRemainingTaskCount());
        assertEquals(createdModel.getNotificationCorrelationId(), model.getNotificationCorrelationId());
    }

    @Test
    void testUpdateTaskCountEmpty() {
        UUID id = UUID.randomUUID();
        Long updatedTaskCount = 5L;
        Optional<JobSubTaskStatusModel> model = accessor.updateTaskCount(id, updatedTaskCount);
        assertTrue(model.isEmpty());
    }

    @Test
    void testUpdateTaskCount() {
        UUID id = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Long remainingTaskCount = 0L;
        Set<Long> notificationIds = Set.of();
        JobSubTaskStatusModel createdModel = accessor.createSubTaskStatus(id, jobId, remainingTaskCount, notificationIds);
        Long updatedTaskCount = 5L;
        Optional<JobSubTaskStatusModel> savedModel = accessor.updateTaskCount(id, updatedTaskCount);
        assertTrue(savedModel.isPresent());
        JobSubTaskStatusModel model = savedModel.get();
        assertEquals(createdModel.getParentEventId(), model.getParentEventId());
        assertEquals(createdModel.getJobId(), model.getJobId());
        assertNotEquals(createdModel.getRemainingTaskCount(), model.getRemainingTaskCount());
        assertEquals(updatedTaskCount, model.getRemainingTaskCount());
        assertEquals(createdModel.getNotificationCorrelationId(), model.getNotificationCorrelationId());
    }

    @Test
    void testDecrementTaskCountEmpty() {
        UUID id = UUID.randomUUID();
        Optional<JobSubTaskStatusModel> model = accessor.decrementTaskCount(id);
        assertTrue(model.isEmpty());
    }

    @Test
    void testDecrementTaskCount() {
        UUID id = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Long remainingTaskCount = 5L;
        Set<Long> notificationIds = Set.of();
        JobSubTaskStatusModel createdModel = accessor.createSubTaskStatus(id, jobId, remainingTaskCount, notificationIds);
        Optional<JobSubTaskStatusModel> savedModel = accessor.decrementTaskCount(id);
        assertTrue(savedModel.isPresent());
        JobSubTaskStatusModel model = savedModel.get();
        assertEquals(createdModel.getParentEventId(), model.getParentEventId());
        assertEquals(createdModel.getJobId(), model.getJobId());
        assertNotEquals(createdModel.getRemainingTaskCount(), model.getRemainingTaskCount());
        assertEquals(4L, model.getRemainingTaskCount());
        assertEquals(createdModel.getNotificationCorrelationId(), model.getNotificationCorrelationId());
    }

    @Test
    void testRemoveTaskEmpty() {
        UUID id = UUID.randomUUID();
        Optional<JobSubTaskStatusModel> model = accessor.removeSubTaskStatus(id);
        assertTrue(model.isEmpty());
    }

    @Test
    void testRemoveTask() {
        UUID id = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Long remainingTaskCount = 5L;
        Set<Long> notificationIds = Set.of();
        accessor.createSubTaskStatus(id, jobId, remainingTaskCount, notificationIds);
        Optional<JobSubTaskStatusModel> removedModel = accessor.removeSubTaskStatus(id);
        assertTrue(removedModel.isPresent());
        assertTrue(accessor.getSubTaskStatus(id).isEmpty());
    }

}
