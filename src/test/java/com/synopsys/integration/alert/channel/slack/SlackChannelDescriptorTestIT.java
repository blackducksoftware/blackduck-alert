package com.synopsys.integration.alert.channel.slack;

import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.channel.DescriptorTestConfigTest;
import com.synopsys.integration.alert.channel.slack.descriptor.SlackDescriptor;
import com.synopsys.integration.alert.channel.slack.mock.MockSlackEntity;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionRepository;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionRepository;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionRepository;
import com.synopsys.integration.alert.database.entity.channel.GlobalChannelConfigEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelationRepositoryAccessor;
import com.synopsys.integration.alert.web.channel.model.SlackDistributionConfig;

public class SlackChannelDescriptorTestIT extends DescriptorTestConfigTest<SlackDistributionConfig, SlackDistributionConfigEntity, GlobalChannelConfigEntity> {
    private final SlackDescriptor slackDescriptor;

    @Autowired
    public SlackChannelDescriptorTestIT(final EmailGroupDistributionRepository emailGroupDistributionRepository,
        final HipChatDistributionRepository hipChatDistributionRepository,
        final SlackDistributionRepository slackDistributionRepository, final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor,
        final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor,
        final UserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor, final SlackDescriptor slackDescriptor) {
        super(emailGroupDistributionRepository, hipChatDistributionRepository, slackDistributionRepository, blackDuckProjectRepositoryAccessor, blackDuckUserRepositoryAccessor, userProjectRelationRepositoryAccessor);
        this.slackDescriptor = slackDescriptor;
    }

    @Override
    public void cleanGlobalRepository() {
        // do nothing no global configuration
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
