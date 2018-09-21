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
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelation;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelationRepositoryAccessor;
import com.synopsys.integration.alert.web.controller.ControllerTest;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;

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

    @Before
    public void testSetup() {
        DatabaseEntity project1 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity("Project one", "", "", ""));
        DatabaseEntity project2 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity("Project two", "", "", ""));
        DatabaseEntity project3 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity("Project three", "", "", ""));
        DatabaseEntity project4 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity("Project four", "", "", ""));

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
        final DatabaseEntity savedEntity = emailGlobalRepositoryAccessor.saveEntity(mockEmailGlobalEntity.createGlobalEntity());
        return savedEntity.getId();
    }

    @Override
    public void deleteGlobalConfig(final long id) {
        emailGlobalRepositoryAccessor.deleteEntity(id);
    }

}
