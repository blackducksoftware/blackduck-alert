package com.blackducksoftware.integration.hub.alert.channel.email;

import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.hub.alert.TestPropertyKey;
import com.blackducksoftware.integration.hub.alert.channel.email.controller.distribution.EmailGroupDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.channel.email.mock.MockEmailRestModel;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.distribution.EmailGroupDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.distribution.EmailGroupDistributionRepository;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailRepository;
import com.blackducksoftware.integration.hub.alert.channel.manager.ChannelManagerTest;

public class EmailChannelManagerTest extends ChannelManagerTest<EmailGroupDistributionRestModel, EmailGroupDistributionConfigEntity, GlobalEmailConfigEntity> {

    @Autowired
    private GlobalEmailRepository globalEmailRepository;

    @Autowired
    private EmailGroupDistributionRepository distributionRepository;

    @Override
    public String getDestination() {
        return EmailGroupChannel.COMPONENT_NAME;
    }

    @Override
    public void cleanGlobalRepository() {
        globalEmailRepository.deleteAll();
    }

    @Override
    public void saveGlobalConfiguration() {
        final String smtpHost = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST);
        final String smtpFrom = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM);
        final GlobalEmailConfigEntity emailGlobalConfigEntity = new GlobalEmailConfigEntity(smtpHost, null, null, null, null, null, null, smtpFrom, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        globalEmailRepository.save(emailGlobalConfigEntity);
    }

    @Override
    public void cleanDistributionRepository() {
        distributionRepository.deleteAll();
    }

    @Override
    public MockEmailRestModel getMockRestModelUtil() {
        return new MockEmailRestModel();
    }

}
