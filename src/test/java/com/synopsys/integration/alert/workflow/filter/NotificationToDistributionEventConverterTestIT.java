package com.synopsys.integration.alert.workflow.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.BlackDuckProjectDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModelBuilder;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.common.workflow.processor.NotificationToDistributionEventConverter;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

@AlertIntegrationTest
public class NotificationToDistributionEventConverterTestIT {
    @Autowired
    private DescriptorMap descriptorMap;
    @Autowired
    private ConfigurationAccessor configurationAccessor;

    @Test
    public void convertToEventsTest() throws Exception {
        NotificationToDistributionEventConverter converter = new NotificationToDistributionEventConverter(configurationAccessor, descriptorMap);
        List<MessageContentGroup> messageContentGroups = new ArrayList<>();
        MessageContentGroup contentGroup1 = MessageContentGroup.singleton(createMessageContent("test"));
        MessageContentGroup contentGroup2 = MessageContentGroup.singleton(createMessageContent("example"));
        messageContentGroups.add(contentGroup1);
        messageContentGroups.add(contentGroup2);

        List<DistributionEvent> emailEvents = converter.convertToEvents(createEmailConfig(), messageContentGroups);
        List<DistributionEvent> slackEvents = converter.convertToEvents(createSlackConfig(), messageContentGroups);
        assertEquals(4, emailEvents.size() + slackEvents.size());
    }

    private DistributionJobModel createEmailConfig() {
        DistributionJobModelBuilder jobBuilder = createJobBuilderWithDefaultBlackDuckFields();
        jobBuilder.channelDescriptorName(ChannelKeys.EMAIL.getUniversalKey());
        EmailJobDetailsModel emailJobDetailsModel = new EmailJobDetailsModel(
            "Alert unit test subject line",
            false,
            true,
            null,
            List.of("noreply@blackducksoftware.com")
        );
        jobBuilder.distributionJobDetails(emailJobDetailsModel);
        return jobBuilder.build();
    }

    private DistributionJobModel createSlackConfig() {
        DistributionJobModelBuilder jobBuilder = createJobBuilderWithDefaultBlackDuckFields();
        jobBuilder.channelDescriptorName(ChannelKeys.SLACK.getUniversalKey());
        SlackJobDetailsModel slackJobDetails = new SlackJobDetailsModel("IT Test Slack Webhook", "IT Test Slack Channel Name", "IT Test Slack Channel Username");
        jobBuilder.distributionJobDetails(slackJobDetails);
        return jobBuilder.build();
    }

    private ProviderMessageContent createMessageContent(String value) throws AlertException {
        return new ProviderMessageContent.Builder()
                   .applyProvider("testProvider", 1L, "testProviderConfig")
                   .applyTopic("Name", value).build();
    }

    private DistributionJobModelBuilder createJobBuilderWithDefaultBlackDuckFields() {
        List<BlackDuckProjectDetailsModel> projectDetails = List.of(
            new BlackDuckProjectDetailsModel("TestProject1", "TestProject1_href"),
            new BlackDuckProjectDetailsModel("TestProject2", "TestProject2_href")
        );

        return DistributionJobModel.builder()
                   .name("Mock Job")
                   .enabled(true)
                   .notificationTypes(List.of(NotificationType.VULNERABILITY.toString(), NotificationType.RULE_VIOLATION.toString()))
                   .processingType(ProcessingType.DEFAULT)
                   .filterByProject(true)
                   .projectNamePattern(".*UnitTest.*")
                   .projectFilterDetails(projectDetails);
    }

}
