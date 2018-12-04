package com.synopsys.integration.alert.web.channel.controller;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.channel.email.EmailGroupChannel;
import com.synopsys.integration.alert.channel.email.mock.MockEmailEntity;
import com.synopsys.integration.alert.channel.email.mock.MockEmailGlobalEntity;
import com.synopsys.integration.alert.channel.email.mock.MockEmailRestModel;
import com.synopsys.integration.alert.database.channel.email.EmailDistributionRepositoryAccessor;
import com.synopsys.integration.alert.database.channel.email.EmailGlobalRepositoryAccessor;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.provider.blackduck.GlobalBlackDuckConfigEntity;
import com.synopsys.integration.alert.database.provider.blackduck.GlobalBlackDuckRepository;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelation;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelationRepositoryAccessor;
import com.synopsys.integration.alert.web.controller.ControllerTest;

public class EmailChannelDistributionControllerTestIT extends ControllerTest {
    @Autowired
    private EmailDistributionRepositoryAccessor emailDistributionRepositoryAccessor;
    @Autowired
    private EmailGlobalRepositoryAccessor emailGlobalRepositoryAccessor;
    @Autowired
    private BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor;
    @Autowired
    private BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor;
    @Autowired
    private UserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor;
    @Autowired
    private GlobalBlackDuckRepository globalBlackDuckRepository;

    @Before
    public void testSetup() {
        final DatabaseEntity project1 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity("Project one", "", "", ""));
        final DatabaseEntity project2 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity("Project two", "", "", ""));
        final DatabaseEntity project3 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity("Project three", "", "", ""));
        final DatabaseEntity project4 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity("Project four", "", "", ""));

        final DatabaseEntity user1 = blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity("noreply@blackducksoftware.com", false));
        final DatabaseEntity user2 = blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity("noreply@blackducksoftware.com", false));
        final DatabaseEntity user3 = blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity("noreply@blackducksoftware.com", false));

        final UserProjectRelation userProjectRelation1 = new UserProjectRelation(user1.getId(), project1.getId());
        final UserProjectRelation userProjectRelation2 = new UserProjectRelation(user1.getId(), project2.getId());
        final UserProjectRelation userProjectRelation3 = new UserProjectRelation(user2.getId(), project3.getId());
        final UserProjectRelation userProjectRelation4 = new UserProjectRelation(user3.getId(), project4.getId());
        userProjectRelationRepositoryAccessor.deleteAndSaveAll(new HashSet<>(Arrays.asList(userProjectRelation1, userProjectRelation2, userProjectRelation3, userProjectRelation4)));

        final GlobalBlackDuckConfigEntity blackDuckConfigEntity = new GlobalBlackDuckConfigEntity(300,
            testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_API_KEY),
            testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_URL));
        globalBlackDuckRepository.deleteAll();
        globalBlackDuckRepository.save(blackDuckConfigEntity);
    }

    @Override
    public EmailDistributionRepositoryAccessor getRepositoryAccessor() {
        return emailDistributionRepositoryAccessor;
    }

    @Override
    public DatabaseEntity getEntity() {
        return new MockEmailEntity().createEntity();
    }

    @Override
    public CommonDistributionConfig getConfig() {
        final MockEmailRestModel mockEmailRestModel = new MockEmailRestModel();
        mockEmailRestModel.setEmailSubjectLine("Controller Test");
        return mockEmailRestModel.createRestModel();
    }

    @Override
    public String getDescriptorName() {
        return EmailGroupChannel.COMPONENT_NAME;
    }

    @Override
    public Long saveGlobalConfig() {
        final MockEmailGlobalEntity mockEmailGlobalEntity = new MockEmailGlobalEntity();
        mockEmailGlobalEntity.setMailSmtpHost(testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST));
        mockEmailGlobalEntity.setMailSmtpFrom(testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM));
        mockEmailGlobalEntity.setMailSmtpUser(testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_USER));
        mockEmailGlobalEntity.setMailSmtpPassword(testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PASSWORD));
        mockEmailGlobalEntity.setMailSmtpEhlo(Boolean.valueOf(testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_EHLO)));
        mockEmailGlobalEntity.setMailSmtpAuth(Boolean.valueOf(testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_AUTH)));
        mockEmailGlobalEntity.setMailSmtpPort(Integer.valueOf(testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PORT)));
        final DatabaseEntity savedEntity = emailGlobalRepositoryAccessor.saveEntity(mockEmailGlobalEntity.createGlobalEntity());
        return savedEntity.getId();
    }

    @Override
    public void deleteGlobalConfig(final long id) {
        emailGlobalRepositoryAccessor.deleteEntity(id);
    }

}
