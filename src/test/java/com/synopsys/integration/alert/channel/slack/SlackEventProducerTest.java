package com.synopsys.integration.alert.channel.slack;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Date;

import org.junit.Test;

import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.model.LinkableItem;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.web.channel.model.SlackDistributionConfig;
import com.synopsys.integration.rest.RestConstants;

public class SlackEventProducerTest {
    @Test
    public void createSlackEvent() throws Exception {
        final Long commonDistributionConfigId = 25L;
        final Long distributionConfigId = 33L;
        final String distributionType = SlackChannel.COMPONENT_NAME;
        final String providerName = BlackDuckProvider.COMPONENT_NAME;
        final String formatType = "FORMAT";

        final String channelUsername = "Slack UserName";
        final String webhook = "WebHook";
        final String channelName = "Alert Channel";

        final SlackEventProducer slackEventProducer = new SlackEventProducer();

        final LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        final AggregateMessageContent content = new AggregateMessageContent("testTopic", "", null, subTopic, Collections.emptyList());

        final SlackChannelEvent expected = new SlackChannelEvent(RestConstants.formatDate(new Date()), providerName, formatType,
            content, commonDistributionConfigId, channelUsername, webhook, channelName);

        SlackDistributionConfig slackDistributionConfig = new SlackDistributionConfig(commonDistributionConfigId.toString(), webhook, channelUsername, channelName, distributionConfigId.toString(), distributionType, "Test HipChat Job",
            providerName, "REAL_TIME", "FALSE", "", Collections.emptyList(), Collections.emptyList(), formatType);

        final SlackChannelEvent event = slackEventProducer.createChannelEvent(slackDistributionConfig, content);
        assertEquals(expected.getAuditEntryId(), event.getAuditEntryId());
        assertEquals(expected.getDestination(), event.getDestination());
        assertEquals(expected.getProvider(), event.getProvider());
        assertEquals(expected.getFormatType(), event.getFormatType());
        assertEquals(expected.getContent(), event.getContent());
        assertEquals(expected.getChannelUsername(), event.getChannelUsername());
        assertEquals(expected.getWebHook(), event.getWebHook());
        assertEquals(expected.getChannelName(), event.getChannelName());

        final AggregateMessageContent testContent = slackEventProducer.createTestNotificationContent();

        final SlackChannelEvent expectedTest = new SlackChannelEvent(RestConstants.formatDate(new Date()), providerName, formatType,
            testContent, commonDistributionConfigId, channelUsername, webhook, channelName);

        slackDistributionConfig = new SlackDistributionConfig("1", webhook, channelUsername, channelName, "1", distributionType, "Test HipChat Job", providerName,
            "REAL_TIME", "FALSE", "", Collections.emptyList(), Collections.emptyList(), "DEFAULT");

        final SlackChannelEvent testEvent = slackEventProducer.createChannelTestEvent(slackDistributionConfig);
        assertEquals(expectedTest.getAuditEntryId(), testEvent.getAuditEntryId());
        assertEquals(expectedTest.getDestination(), testEvent.getDestination());
        assertEquals(expectedTest.getProvider(), testEvent.getProvider());
        assertEquals(expectedTest.getContent(), testEvent.getContent());
        assertEquals(expectedTest.getChannelUsername(), testEvent.getChannelUsername());
        assertEquals(expectedTest.getWebHook(), testEvent.getWebHook());
        assertEquals(expectedTest.getChannelName(), testEvent.getChannelName());
    }
}
