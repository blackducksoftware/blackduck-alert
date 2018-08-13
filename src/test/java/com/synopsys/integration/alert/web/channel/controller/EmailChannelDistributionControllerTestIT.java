package com.synopsys.integration.alert.web.channel.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.channel.email.EmailGroupChannel;
import com.synopsys.integration.alert.channel.email.mock.MockEmailEntity;
import com.synopsys.integration.alert.channel.email.mock.MockEmailRestModel;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionRepository;
import com.synopsys.integration.alert.mock.entity.MockEntityUtil;
import com.synopsys.integration.alert.mock.model.MockRestModelUtil;
import com.synopsys.integration.alert.web.channel.model.EmailDistributionConfig;
import com.synopsys.integration.alert.web.controller.ControllerTest;

public class EmailChannelDistributionControllerTestIT extends ControllerTest<EmailGroupDistributionConfigEntity, EmailDistributionConfig, EmailGroupDistributionRepository> {

    @Autowired
    private EmailGroupDistributionRepository emailGroupDistributionRepository;

    @Override
    public EmailGroupDistributionRepository getEntityRepository() {
        return emailGroupDistributionRepository;
    }

    @Override
    public MockEntityUtil<EmailGroupDistributionConfigEntity> getEntityMockUtil() {
        return new MockEmailEntity();
    }

    @Override
    public MockRestModelUtil<EmailDistributionConfig> getRestModelMockUtil() {
        return new MockEmailRestModel();
    }

    @Override
    public String getDescriptorName() {
        return EmailGroupChannel.COMPONENT_NAME;
    }

}
