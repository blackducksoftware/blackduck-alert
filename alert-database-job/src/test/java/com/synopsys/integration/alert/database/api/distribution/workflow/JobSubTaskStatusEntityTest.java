package com.synopsys.integration.alert.database.api.distribution.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.database.distribution.workflow.JobSubTaskStatusEntity;

class JobSubTaskStatusEntityTest {
    @Test
    void defaultConstructorTest() {
        JobSubTaskStatusEntity entity = new JobSubTaskStatusEntity();
        assertNull(entity.getId());
        assertNull(entity.getJobId());
        assertNull(entity.getNotificationCorrelationId());
        assertNull(entity.getRemainingEvents());
    }

    @Test
    void constructorTest() {
        UUID id = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        Long remainingTaskCount = 5L;
        JobSubTaskStatusEntity entity = new JobSubTaskStatusEntity(id, jobId, remainingTaskCount, correlationId);
        assertEquals(id, entity.getId());
        assertEquals(jobId, entity.getJobId());
        assertEquals(correlationId, entity.getNotificationCorrelationId());
        assertEquals(remainingTaskCount, entity.getRemainingEvents());
    }
}
