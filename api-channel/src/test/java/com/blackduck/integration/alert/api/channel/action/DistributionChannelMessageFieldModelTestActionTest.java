/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.action;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.channel.DistributionChannel;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKey;
import com.blackduck.integration.alert.common.enumeration.FrequencyType;
import com.blackduck.integration.alert.common.enumeration.ProcessingType;
import com.blackduck.integration.alert.common.message.model.MessageResult;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;

class DistributionChannelMessageFieldModelTestActionTest {
    private static final String CLASS_NAME = DistributionChannelMessageFieldModelTestActionTest.class.getSimpleName();

    @Test
    void testConfigTest() throws AlertException {
        String expectedStatusMessage = "Test Result";
        DistributionChannelMessageTestAction<MockDistributionJobDetailsModel> testAction = createTestAction(expectedStatusMessage);

        DistributionJobModel distributionJobModel = createDistributionJobModel();
        MessageResult messageResult = testAction.testConfig(distributionJobModel, "jobName", "Test Topic", "Test Message");
        assertEquals(expectedStatusMessage, messageResult.getStatusMessage());
    }

    @Test
    void testConfigWithNullParamsTest() throws AlertException {
        String expectedStatusMessage = "Test Result for null test";
        DistributionChannelMessageTestAction<MockDistributionJobDetailsModel> testAction = createTestAction(expectedStatusMessage);

        DistributionJobModel distributionJobModel = createDistributionJobModel();
        MessageResult messageResult = testAction.testConfig(distributionJobModel, "jobName", null, null);
        assertEquals(expectedStatusMessage, messageResult.getStatusMessage());

    }

    private DistributionChannelMessageTestAction<MockDistributionJobDetailsModel> createTestAction(String expectedStatusMessage) {
        DistributionChannel<MockDistributionJobDetailsModel> distributionChannel = createDistributionChannel(expectedStatusMessage);
        return new DistributionChannelMessageTestAction<>(
            MockDistributionJobDetailsModel.DEFAULT_CHANNEL_KEY,
            distributionChannel
        ) {};
    }

    private DistributionChannel<MockDistributionJobDetailsModel> createDistributionChannel(String expectedStatusMessage) {
        return (distributionDetails, messages, jobName, jobId, jobExecutionId, notificationIds) -> new MessageResult(expectedStatusMessage);
    }

    private DistributionJobModel createDistributionJobModel() {
        return DistributionJobModel.builder()
            .jobId(UUID.randomUUID())
            .name(CLASS_NAME)
            .distributionFrequency(FrequencyType.REAL_TIME)
            .processingType(ProcessingType.SUMMARY)
            .channelDescriptorName(MockDistributionJobDetailsModel.DEFAULT_CHANNEL_KEY.getUniversalKey())
            .createdAt(OffsetDateTime.now())
            .blackDuckGlobalConfigId(0L)
            .notificationTypes(List.of("irrelevant_string"))
            .filterByProject(false)
            .build();
    }

    private static class MockDistributionJobDetailsModel extends DistributionJobDetailsModel {
        private static final ChannelKey DEFAULT_CHANNEL_KEY = new ChannelKey(CLASS_NAME, CLASS_NAME);

        public MockDistributionJobDetailsModel(UUID jobId) {
            super(DEFAULT_CHANNEL_KEY, jobId);
        }

    }

}
