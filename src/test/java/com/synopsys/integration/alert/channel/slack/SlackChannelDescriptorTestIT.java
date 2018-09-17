package com.synopsys.integration.alert.channel.slack;

import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.channel.DescriptorTestConfigTest;
import com.synopsys.integration.alert.channel.event.ChannelEventFactory;
import com.synopsys.integration.alert.channel.slack.descriptor.SlackDescriptor;
import com.synopsys.integration.alert.channel.slack.mock.MockSlackEntity;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.database.channel.email.EmailDistributionRepositoryAccessor;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionRepositoryAccessor;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionRepositoryAccessor;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.entity.channel.GlobalChannelConfigEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelationRepositoryAccessor;
import com.synopsys.integration.alert.web.channel.model.SlackDistributionConfig;

public class SlackChannelDescriptorTestIT extends DescriptorTestConfigTest<SlackDistributionConfig, SlackDistributionConfigEntity, GlobalChannelConfigEntity> {
    @Autowired
    private EmailDistributionRepositoryAccessor emailDistributionRepositoryAccessor;
    @Autowired
    private HipChatDistributionRepositoryAccessor hipChatDistributionRepositoryAccessor;
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
    public DatabaseEntity getDistributionEntity() {
        final MockSlackEntity mockSlackEntity = new MockSlackEntity();
        final SlackDistributionConfigEntity slackDistributionConfigEntity = mockSlackEntity.createEntity();
        return slackDistributionRepositoryAccessor.saveEntity(slackDistributionConfigEntity);
    }

    @Override
    public ChannelEventFactory createChannelEventFactory() {
        return new ChannelEventFactory(emailDistributionRepositoryAccessor, hipChatDistributionRepositoryAccessor, slackDistributionRepositoryAccessor,
            blackDuckProjectRepositoryAccessor, blackDuckUserRepositoryAccessor, userProjectRelationRepositoryAccessor);
    }

    @Override
    public void cleanGlobalRepository() {
        // do nothing no global configuration
    }

    @Override
    public void cleanDistributionRepositories() {
        emailDistributionRepositoryAccessor.deleteAll();
        hipChatDistributionRepositoryAccessor.deleteAll();
        slackDistributionRepositoryAccessor.deleteAll();
    }

    @Override
    public void saveGlobalConfiguration() {
        // do nothing no global configuration
    }

    @Override
    public MockSlackEntity getMockEntityUtil() {
        final MockSlackEntity restModel = new MockSlackEntity();
        restModel.setChannelName(this.properties.getProperty(TestPropertyKey.TEST_SLACK_CHANNEL_NAME));
        restModel.setChannelUsername(this.properties.getProperty(TestPropertyKey.TEST_SLACK_USERNAME));
        restModel.setWebhook(this.properties.getProperty(TestPropertyKey.TEST_SLACK_WEBHOOK));
        return restModel;
    }

    @Override
    public ChannelDescriptor getDescriptor() {
        return slackDescriptor;
    }

}
