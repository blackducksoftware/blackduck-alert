package com.synopsys.integration.alert.database.api.distribution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.persistence.model.job.workflow.JobSubTaskStatusModel;
import com.synopsys.integration.alert.database.api.workflow.DefaultJobSubTaskAccessor;

class DefaultJobSubTaskAccessorTest {

    @Test
    void testCreateSubTask() {
        MockJobSubTaskStatusRepository subTaskRepository = new MockJobSubTaskStatusRepository();
        MockCorrelationToNotificationRelationRepository relationRepository = new MockCorrelationToNotificationRelationRepository();

        DefaultJobSubTaskAccessor accessor = new DefaultJobSubTaskAccessor(subTaskRepository, relationRepository);
        UUID id = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Long remainingTaskCount = 0L;
        Set<Long> notificationIds = Set.of();
        JobSubTaskStatusModel model = accessor.createSubTaskStatus(id, jobId, remainingTaskCount, notificationIds);
        assertNotNull(model);
        assertEquals(id, model.getParentEventId());
        assertEquals(id, model.getParentEventId());
        assertEquals(id, model.getParentEventId());
    }
}
