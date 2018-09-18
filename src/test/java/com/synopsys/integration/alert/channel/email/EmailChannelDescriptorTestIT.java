package com.synopsys.integration.alert.channel.email;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.channel.DescriptorTestConfigTest;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.channel.email.mock.MockEmailEntity;
import com.synopsys.integration.alert.channel.event.ChannelEventFactory;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.database.channel.email.EmailDistributionRepositoryAccessor;
import com.synopsys.integration.alert.database.channel.email.EmailGlobalConfigEntity;
import com.synopsys.integration.alert.database.channel.email.EmailGlobalRepository;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionRepositoryAccessor;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionRepositoryAccessor;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelationRepositoryAccessor;
import com.synopsys.integration.alert.web.channel.model.EmailDistributionConfig;

public class EmailChannelDescriptorTestIT extends DescriptorTestConfigTest<EmailDistributionConfig, EmailGroupDistributionConfigEntity, EmailGlobalConfigEntity> {
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
    private EmailGlobalRepository emailGlobalRepository;
    @Autowired
    private EmailDescriptor emailDescriptor;
    @Autowired
    private Gson gson;

    @Override
    public DatabaseEntity getDistributionEntity() {
        final MockEmailEntity mockEmailEntity = new MockEmailEntity();
        final EmailGroupDistributionConfigEntity emailGroupDistributionConfigEntity = mockEmailEntity.createEntity();
        return emailDistributionRepositoryAccessor.saveEntity(emailGroupDistributionConfigEntity);
    }

    @Override
    public ChannelEventFactory createChannelEventFactory() {
        return new ChannelEventFactory(emailDistributionRepositoryAccessor, hipChatDistributionRepositoryAccessor, slackDistributionRepositoryAccessor,
            blackDuckProjectRepositoryAccessor, blackDuckUserRepositoryAccessor, userProjectRelationRepositoryAccessor, gson);
    }

    @Override
    public void cleanGlobalRepository() {
        emailGlobalRepository.deleteAll();
    }

    @Override
    public void cleanDistributionRepositories() {
        emailDistributionRepositoryAccessor.deleteAll();
        hipChatDistributionRepositoryAccessor.deleteAll();
        slackDistributionRepositoryAccessor.deleteAll();
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
