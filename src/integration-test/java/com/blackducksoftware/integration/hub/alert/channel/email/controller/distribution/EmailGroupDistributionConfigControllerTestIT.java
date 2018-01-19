package com.blackducksoftware.integration.hub.alert.channel.email.controller.distribution;

import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.hub.alert.channel.email.controller.distribution.EmailGroupDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.channel.email.mock.MockEmailEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.mock.MockEmailRestModel;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.distribution.EmailGroupDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.distribution.EmailGroupDistributionRepository;
import com.blackducksoftware.integration.hub.alert.web.controller.ControllerTest;

public class EmailGroupDistributionConfigControllerTestIT extends ControllerTest<EmailGroupDistributionConfigEntity, EmailGroupDistributionRestModel, EmailGroupDistributionRepository> {

    @Autowired
    EmailGroupDistributionRepository emailGroupDistributionRepository;

    @Override
    public EmailGroupDistributionRepository getEntityRepository() {
        return emailGroupDistributionRepository;
    }

    @Override
    public MockEmailEntity getEntityMockUtil() {
        return new MockEmailEntity();
    }

    @Override
    public MockEmailRestModel getRestModelMockUtil() {
        return new MockEmailRestModel();
    }

    @Override
    public String getRestControllerUrl() {
        return "/configuration/distribution/emailGroup";
    }

}
