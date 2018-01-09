package com.blackducksoftware.integration.hub.alert.web.controller.distribution;

import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.EmailGroupDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.EmailGroupDistributionRepository;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockEmailEntity;
import com.blackducksoftware.integration.hub.alert.mock.model.MockEmailRestModel;
import com.blackducksoftware.integration.hub.alert.web.controller.ControllerTest;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.EmailGroupDistributionRestModel;

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
