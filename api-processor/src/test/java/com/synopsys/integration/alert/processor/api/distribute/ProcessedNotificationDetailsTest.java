package com.synopsys.integration.alert.processor.api.distribute;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModelBuilder;

public class ProcessedNotificationDetailsTest {
    private static final String CHANNEL_NAME = "TestChannelName";
    private static final String JOB_NAME = "TestJobName";
    private final UUID uuid = UUID.randomUUID();

    @Test
    public void getJobIdTest() {
        ProcessedNotificationDetails processedNotificationDetails = new ProcessedNotificationDetails(uuid, CHANNEL_NAME, JOB_NAME);
        assertEquals(uuid, processedNotificationDetails.getJobId());
    }

    @Test
    public void getChannelNameTest() {
        ProcessedNotificationDetails processedNotificationDetails = new ProcessedNotificationDetails(uuid, CHANNEL_NAME, JOB_NAME);
        assertEquals(CHANNEL_NAME, processedNotificationDetails.getChannelName());
    }

    @Test
    public void getJobName() {
        ProcessedNotificationDetails processedNotificationDetails = new ProcessedNotificationDetails(uuid, CHANNEL_NAME, JOB_NAME);
        assertEquals(JOB_NAME, processedNotificationDetails.getJobName());
    }

    @Test
    public void fromDistributionJobTest() {
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

        ProcessedNotificationDetails processedNotificationDetails = ProcessedNotificationDetails.fromDistributionJob(distributionJobModel);
        assertEquals(uuid, processedNotificationDetails.getJobId());
        assertEquals(CHANNEL_NAME, processedNotificationDetails.getChannelName());
        assertEquals(JOB_NAME, processedNotificationDetails.getJobName());
    }
}
