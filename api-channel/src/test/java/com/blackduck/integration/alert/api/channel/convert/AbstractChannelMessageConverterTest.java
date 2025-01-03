/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.channel.convert.mock.MockChannelMessageFormatter;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKey;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderDetails;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderMessageHolder;
import com.blackduck.integration.alert.api.processor.extract.model.SimpleMessage;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectMessage;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectOperation;
import com.blackduck.integration.alert.common.message.model.LinkableItem;
import com.blackduck.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;

public class AbstractChannelMessageConverterTest {
    private static final String CLASS_NAME = AbstractChannelMessageConverterTest.class.getSimpleName();

    @Test
    public void convertToChannelMessagesTest() {
        MockChannelMessageFormatter mockChannelMessageFormatter = new MockChannelMessageFormatter(Integer.MAX_VALUE);
        MockChannelMessageConverter mockChannelMessageConverter = new MockChannelMessageConverter(mockChannelMessageFormatter);

        MockDistributionJobDetailsModel jobDetails = new MockDistributionJobDetailsModel(UUID.randomUUID());
        ProviderDetails providerDetails = new ProviderDetails(0L, new LinkableItem("Provider", "Black Duck"));

        ProjectMessage projectCreateMessage = ProjectMessage.projectStatusInfo(providerDetails, new LinkableItem("Project", "A project"), ProjectOperation.CREATE);
        SimpleMessage simpleMessage = SimpleMessage.original(providerDetails, "Summary", "Description", List.of());
        ProviderMessageHolder providerMessageHolder = new ProviderMessageHolder(List.of(projectCreateMessage), List.of(simpleMessage));

        List<MockOutputModel> mockOutputModels = mockChannelMessageConverter.convertToChannelMessages(jobDetails, providerMessageHolder, "jobName");
        assertEquals(2, mockOutputModels.size());

        for (MockOutputModel mockOutputModel : mockOutputModels) {
            assertEquals(1, mockOutputModel.getMessagePieces().size());
        }
    }

    private static class MockChannelMessageConverter extends AbstractChannelMessageConverter<MockDistributionJobDetailsModel, MockOutputModel> {
        public MockChannelMessageConverter(ChannelMessageFormatter channelMessageFormatter) {
            super(channelMessageFormatter);
        }

        @Override
        protected List<MockOutputModel> convertSimpleMessageToChannelMessages(MockDistributionJobDetailsModel distributionDetails, SimpleMessage simpleMessage, List<String> messageChunks) {
            MockOutputModel outputModel = new MockOutputModel(messageChunks);
            return List.of(outputModel);
        }

        @Override
        protected List<MockOutputModel> convertProjectMessageToChannelMessages(MockDistributionJobDetailsModel distributionDetails, ProjectMessage projectMessage, List<String> messageChunks) {
            MockOutputModel outputModel = new MockOutputModel(messageChunks);
            return List.of(outputModel);
        }

    }

    private static class MockDistributionJobDetailsModel extends DistributionJobDetailsModel {
        private static final ChannelKey DEFAULT_CHANNEL_KEY = new ChannelKey(CLASS_NAME, CLASS_NAME);

        public MockDistributionJobDetailsModel(UUID jobId) {
            super(DEFAULT_CHANNEL_KEY, jobId);
        }

    }

    private static class MockOutputModel {
        private final List<String> messagePieces;

        public MockOutputModel(List<String> messagePieces) {
            this.messagePieces = messagePieces;
        }

        public List<String> getMessagePieces() {
            return messagePieces;
        }

    }

}
