package com.synopsys.integration.alert.channel.hipchat;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Date;

import org.junit.Test;

import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.model.LinkableItem;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.web.channel.model.HipChatDistributionConfig;
import com.synopsys.integration.rest.RestConstants;

public class HipChatEventProducerTest {

    @Test
    public void createHipChatEvent() throws Exception {
        final Long commonDistributionConfigId = 25L;
        final Long distributionConfigId = 33L;
        final String distributionType = HipChatChannel.COMPONENT_NAME;
        final String providerName = BlackDuckProvider.COMPONENT_NAME;
        final String formatType = "FORMAT";

        final Integer roomId = 100484;
        final Boolean notify = false;
        final String color = "red";

        final HipChatEventProducer hipChatEventProducer = new HipChatEventProducer();

        final LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        final AggregateMessageContent content = new AggregateMessageContent("testTopic", "", null, subTopic, Collections.emptyList());

        final HipChatChannelEvent expected = new HipChatChannelEvent(RestConstants.formatDate(new Date()), providerName, formatType,
            content, commonDistributionConfigId, roomId, notify, color);

        HipChatDistributionConfig hipChatDistributionConfig = new HipChatDistributionConfig(commonDistributionConfigId.toString(), roomId.toString(), notify, color, distributionConfigId.toString(),
            distributionType, "Test HipChat Job", providerName, "REAL_TIME", "FALSE", "", Collections.emptyList(), Collections.emptyList(), formatType);

        final HipChatChannelEvent event = hipChatEventProducer.createChannelEvent(hipChatDistributionConfig, content);
        assertEquals(expected.getAuditEntryId(), event.getAuditEntryId());
        assertEquals(expected.getDestination(), event.getDestination());
        assertEquals(expected.getProvider(), event.getProvider());
        assertEquals(expected.getFormatType(), event.getFormatType());
        assertEquals(expected.getContent(), event.getContent());
        assertEquals(expected.getRoomId(), event.getRoomId());
        assertEquals(expected.getNotify(), event.getNotify());
        assertEquals(expected.getColor(), event.getColor());

        final AggregateMessageContent testContent = hipChatEventProducer.createTestNotificationContent();

        final HipChatChannelEvent expectedTest = new HipChatChannelEvent(RestConstants.formatDate(new Date()), providerName, formatType, testContent, commonDistributionConfigId, roomId, notify, color);

        hipChatDistributionConfig = new HipChatDistributionConfig(commonDistributionConfigId.toString(), roomId.toString(), notify, color, distributionConfigId.toString(),
            distributionType, "Test HipChat Job", providerName, "REAL_TIME", "FALSE", "", Collections.emptyList(), Collections.emptyList(), "DEFAULT");

        final HipChatChannelEvent testEvent = hipChatEventProducer.createChannelTestEvent(hipChatDistributionConfig);
        assertEquals(expectedTest.getAuditEntryId(), testEvent.getAuditEntryId());
        assertEquals(expectedTest.getDestination(), testEvent.getDestination());
        assertEquals(expectedTest.getProvider(), testEvent.getProvider());
        assertEquals(expectedTest.getContent(), testEvent.getContent());
        assertEquals(expectedTest.getRoomId(), testEvent.getRoomId());
        assertEquals(expectedTest.getNotify(), testEvent.getNotify());
        assertEquals(expectedTest.getColor(), testEvent.getColor());
    }
}
