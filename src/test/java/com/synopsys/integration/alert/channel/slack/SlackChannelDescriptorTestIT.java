package com.synopsys.integration.alert.channel.slack;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.channel.DescriptorTestConfigTest;
import com.synopsys.integration.alert.channel.event.ChannelEventFactory;
import com.synopsys.integration.alert.channel.slack.descriptor.SlackDescriptor;
import com.synopsys.integration.alert.channel.slack.mock.MockSlackEntity;
import com.synopsys.integration.alert.channel.slack.mock.MockSlackRestModel;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.model.LinkableItem;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionRepositoryAccessor;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.entity.channel.GlobalChannelConfigEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelationRepositoryAccessor;
import com.synopsys.integration.alert.mock.model.MockRestModelUtil;
import com.synopsys.integration.alert.web.channel.model.SlackDistributionConfig;

public class SlackChannelDescriptorTestIT extends DescriptorTestConfigTest<SlackDistributionConfig, SlackDistributionConfigEntity, GlobalChannelConfigEntity> {
    @Autowired
    private SlackDistributionRepositoryAccessor slackDistributionRepositoryAccessor;
    @Autowired
    private BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor;
    @Autowired
    private BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor;
    @Autowired
    private UserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor;
    @Autowired
    private SlackDescriptor slackDescriptor;

    @Override
    @Test
    public void testCreateChannelEvent() throws Exception {
        final LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        final AggregateMessageContent content = new AggregateMessageContent("testTopic", "", null, subTopic, Collections.emptyList());
        final DatabaseEntity distributionEntity = getDistributionEntity();
        final String webhook = "Webhook";
        final String channelUsername = "Username";
        final String channelName = "channel";
        final SlackDistributionConfig slackDistributionConfig = new SlackDistributionConfig("1", webhook, channelUsername, channelName,
            String.valueOf(distributionEntity.getId()), getDescriptor().getDestinationName(), "Test Job", "provider", FrequencyType.DAILY.name(), "true",
            Collections.emptyList(), Collections.emptyList(), FormatType.DIGEST.name());

        final SlackChannelEvent channelEvent = (SlackChannelEvent) channelEventFactory.createChannelEvent(slackDistributionConfig, content);

        assertEquals(Long.valueOf(1L), channelEvent.getCommonDistributionConfigId());
        assertEquals(36, channelEvent.getEventId().length());
        assertEquals(getDescriptor().getDestinationName(), channelEvent.getDestination());
        assertEquals(webhook, channelEvent.getWebHook());
        assertEquals(channelUsername, channelEvent.getChannelUsername());
        assertEquals(channelName, channelEvent.getChannelName());
    }

    @Override
    public DatabaseEntity getDistributionEntity() {
        final MockSlackEntity mockSlackEntity = new MockSlackEntity();
        final SlackDistributionConfigEntity slackDistributionConfigEntity = mockSlackEntity.createEntity();
        return slackDistributionRepositoryAccessor.saveEntity(slackDistributionConfigEntity);
    }

    @Override
    public ChannelEventFactory createChannelEventFactory() {
        return new ChannelEventFactory(blackDuckProjectRepositoryAccessor, blackDuckUserRepositoryAccessor, userProjectRelationRepositoryAccessor);
    }

    @Override
    public void cleanGlobalRepository() {
        // do nothing no global configuration
    }

    @Override
    public void cleanDistributionRepositories() {
        slackDistributionRepositoryAccessor.deleteAll();
    }

    @Override
    public void saveGlobalConfiguration() {
        // do nothing no global configuration
    }

    @Override
    public ChannelDescriptor getDescriptor() {
        return slackDescriptor;
    }

    @Override
    public MockRestModelUtil<SlackDistributionConfig> getMockRestModelUtil() {
        final MockSlackRestModel restModel = new MockSlackRestModel();
        restModel.setChannelName(this.properties.getProperty(TestPropertyKey.TEST_SLACK_CHANNEL_NAME));
        restModel.setChannelUsername(this.properties.getProperty(TestPropertyKey.TEST_SLACK_USERNAME));
        restModel.setWebhook(this.properties.getProperty(TestPropertyKey.TEST_SLACK_WEBHOOK));
        return restModel;
    }

}
