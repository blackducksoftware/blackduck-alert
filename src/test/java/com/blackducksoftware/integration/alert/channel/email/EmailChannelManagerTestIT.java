package com.blackducksoftware.integration.alert.channel.email;

import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.alert.TestPropertyKey;
import com.blackducksoftware.integration.alert.channel.ChannelManagerTest;
import com.blackducksoftware.integration.alert.channel.email.mock.MockEmailRestModel;
import com.blackducksoftware.integration.alert.channel.email.model.EmailGroupDistributionConfigEntity;
import com.blackducksoftware.integration.alert.channel.email.model.EmailGroupDistributionRepository;
import com.blackducksoftware.integration.alert.channel.email.model.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.alert.channel.email.model.GlobalEmailRepository;
import com.blackducksoftware.integration.alert.descriptor.ChannelDescriptor;
import com.blackducksoftware.integration.alert.web.channel.model.EmailGroupDistributionRestModel;

public class EmailChannelManagerTestIT extends ChannelManagerTest<EmailGroupDistributionRestModel, EmailGroupDistributionConfigEntity, GlobalEmailConfigEntity> {

    @Autowired
    private GlobalEmailRepository globalEmailRepository;

    @Autowired
    private EmailGroupDistributionRepository distributionRepository;

    @Autowired
    private EmailDescriptor emailDescriptor;

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

    @Override
    public ChannelDescriptor getDescriptor() {
        return emailDescriptor;
    }

}
