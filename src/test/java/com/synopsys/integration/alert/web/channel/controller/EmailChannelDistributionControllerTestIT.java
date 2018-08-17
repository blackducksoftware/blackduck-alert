package com.synopsys.integration.alert.web.channel.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.channel.email.EmailGroupChannel;
import com.synopsys.integration.alert.channel.email.mock.MockEmailEntity;
import com.synopsys.integration.alert.channel.email.mock.MockEmailGlobalEntity;
import com.synopsys.integration.alert.channel.email.mock.MockEmailRestModel;
import com.synopsys.integration.alert.database.channel.email.EmailDistributionRepositoryAccessor;
import com.synopsys.integration.alert.database.channel.email.EmailGlobalRepositoryAccessor;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.web.controller.ControllerTest;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;

public class EmailChannelDistributionControllerTestIT extends ControllerTest {

    @Autowired
    private EmailDistributionRepositoryAccessor emailDistributionRepositoryAccessor;

    @Autowired
    private EmailGlobalRepositoryAccessor emailGlobalRepositoryAccessor;

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
        mockEmailRestModel.setGroupName(testProperties.getProperty(TestPropertyKey.TEST_EMAIL_GROUP));
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
