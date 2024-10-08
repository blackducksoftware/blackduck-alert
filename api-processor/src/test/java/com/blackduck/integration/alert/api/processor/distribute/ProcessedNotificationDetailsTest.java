package com.blackduck.integration.alert.api.processor.distribute;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModelBuilder;

class ProcessedNotificationDetailsTest {
    private static final String CHANNEL_NAME = "TestChannelName";
    private static final String JOB_NAME = "TestJobName";
    private final UUID uuid = UUID.randomUUID();
    private final UUID jobExecutionId = UUID.randomUUID();

    @Test
    void getJobIdTest() {
        ProcessedNotificationDetails processedNotificationDetails = new ProcessedNotificationDetails(jobExecutionId, uuid, CHANNEL_NAME, JOB_NAME);
        assertEquals(uuid, processedNotificationDetails.getJobId());
    }

    @Test
    void getChannelNameTest() {
        ProcessedNotificationDetails processedNotificationDetails = new ProcessedNotificationDetails(jobExecutionId, uuid, CHANNEL_NAME, JOB_NAME);
        assertEquals(CHANNEL_NAME, processedNotificationDetails.getChannelName());
    }

    @Test
    void getJobName() {
        ProcessedNotificationDetails processedNotificationDetails = new ProcessedNotificationDetails(jobExecutionId, uuid, CHANNEL_NAME, JOB_NAME);
        assertEquals(JOB_NAME, processedNotificationDetails.getJobName());
    }

    @Test
    void fromDistributionJobTest() {
        DistributionJobModel distributionJobModel = new DistributionJobModelBuilder()
            .jobId(uuid)
            .channelDescriptorName(CHANNEL_NAME)
            .name(JOB_NAME)
            .distributionFrequency(FrequencyType.REAL_TIME)
            .processingType(ProcessingType.DEFAULT)
            .createdAt(OffsetDateTime.now().minusMinutes(1L))
            .lastUpdated(OffsetDateTime.now())
            .blackDuckGlobalConfigId(10L)
            .notificationTypes(List.of("notificationTypes"))
            .build();

        ProcessedNotificationDetails processedNotificationDetails = ProcessedNotificationDetails.fromDistributionJob(jobExecutionId, distributionJobModel);
        assertEquals(uuid, processedNotificationDetails.getJobId());
        assertEquals(CHANNEL_NAME, processedNotificationDetails.getChannelName());
        assertEquals(JOB_NAME, processedNotificationDetails.getJobName());
        assertEquals(jobExecutionId, processedNotificationDetails.getJobExecutionId());
    }
}
