package com.synopsys.integration.alert.channel.email;

import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.channel.DescriptorTestConfigTest;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.channel.email.mock.MockEmailEntity;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.database.channel.email.EmailGlobalConfigEntity;
import com.synopsys.integration.alert.database.channel.email.EmailGlobalRepository;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionRepository;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionRepository;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionRepository;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelationRepositoryAccessor;
import com.synopsys.integration.alert.web.channel.model.EmailDistributionConfig;

public class EmailChannelDescriptorTestIT extends DescriptorTestConfigTest<EmailDistributionConfig, EmailGroupDistributionConfigEntity, EmailGlobalConfigEntity> {
    private final EmailGlobalRepository emailGlobalRepository;
    private final EmailDescriptor emailDescriptor;

    @Autowired
    public EmailChannelDescriptorTestIT(final EmailGroupDistributionRepository emailGroupDistributionRepository,
        final HipChatDistributionRepository hipChatDistributionRepository,
        final SlackDistributionRepository slackDistributionRepository,
        final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor,
        final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor,
        final UserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor, final EmailGlobalRepository emailGlobalRepository, final EmailDescriptor emailDescriptor) {
        super(emailGroupDistributionRepository, hipChatDistributionRepository, slackDistributionRepository, blackDuckProjectRepositoryAccessor, blackDuckUserRepositoryAccessor, userProjectRelationRepositoryAccessor);
        this.emailGlobalRepository = emailGlobalRepository;
        this.emailDescriptor = emailDescriptor;
    }

    @Override
    public void cleanGlobalRepository() {
        emailGlobalRepository.deleteAll();
    }

    @Override
    public void saveGlobalConfiguration() {
        final String smtpHost = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST);
        final String smtpFrom = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM);
        final EmailGlobalConfigEntity emailGlobalConfigEntity = new EmailGlobalConfigEntity(smtpHost, null, null, null, null, null, null, smtpFrom, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        emailGlobalRepository.save(emailGlobalConfigEntity);
    }

    @Override
    public MockEmailEntity getMockEntityUtil() {
        return new MockEmailEntity();
    }

    @Override
    public ChannelDescriptor getDescriptor() {
        return emailDescriptor;
    }

}
