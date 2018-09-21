package com.synopsys.integration.alert.channel.email;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.channel.DescriptorTestConfigTest;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.channel.email.mock.MockEmailEntity;
import com.synopsys.integration.alert.channel.email.mock.MockEmailRestModel;
import com.synopsys.integration.alert.channel.event.ChannelEventFactory;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.database.channel.email.EmailDistributionRepositoryAccessor;
import com.synopsys.integration.alert.database.channel.email.EmailGlobalConfigEntity;
import com.synopsys.integration.alert.database.channel.email.EmailGlobalRepository;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionRepositoryAccessor;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionRepositoryAccessor;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelation;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelationRepositoryAccessor;
import com.synopsys.integration.alert.mock.model.MockRestModelUtil;
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

    @Before
    public void testSetup() {
        DatabaseEntity project1 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity("Project one", "", ""));
        DatabaseEntity project2 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity("Project two", "", ""));
        DatabaseEntity project3 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity("Project three", "", ""));
        DatabaseEntity project4 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity("Project four", "", ""));

        DatabaseEntity user1 = blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity("email1", false));
        DatabaseEntity user2 = blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity("email2", false));
        DatabaseEntity user3 = blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity("email3", false));

        UserProjectRelation userProjectRelation1 = new UserProjectRelation(user1.getId(), project1.getId());
        UserProjectRelation userProjectRelation2 = new UserProjectRelation(user1.getId(), project2.getId());
        UserProjectRelation userProjectRelation3 = new UserProjectRelation(user2.getId(), project3.getId());
        UserProjectRelation userProjectRelation4 = new UserProjectRelation(user3.getId(), project4.getId());
        userProjectRelationRepositoryAccessor.deleteAndSaveAll(new HashSet<>(Arrays.asList(userProjectRelation1, userProjectRelation2, userProjectRelation3, userProjectRelation4)));
    }

    @Override
    public DatabaseEntity getDistributionEntity() {
        final MockEmailEntity mockEmailEntity = new MockEmailEntity();
        final EmailGroupDistributionConfigEntity emailGroupDistributionConfigEntity = mockEmailEntity.createEntity();
        return emailDistributionRepositoryAccessor.saveEntity(emailGroupDistributionConfigEntity);
    }

    @Override
    public ChannelEventFactory createChannelEventFactory() {
        return new ChannelEventFactory(emailDistributionRepositoryAccessor, hipChatDistributionRepositoryAccessor, slackDistributionRepositoryAccessor,
            blackDuckProjectRepositoryAccessor, blackDuckUserRepositoryAccessor, userProjectRelationRepositoryAccessor);
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
    public ChannelDescriptor getDescriptor() {
        return emailDescriptor;
    }

    @Override
    public MockRestModelUtil<EmailDistributionConfig> getMockRestModelUtil() {
        return new MockEmailRestModel();
    }

}
