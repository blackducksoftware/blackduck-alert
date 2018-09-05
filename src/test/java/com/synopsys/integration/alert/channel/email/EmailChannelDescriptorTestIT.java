package com.synopsys.integration.alert.channel.email;

import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.channel.DescriptorTestConfigTest;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.channel.email.mock.MockEmailEntity;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.database.channel.email.EmailGlobalConfigEntity;
import com.synopsys.integration.alert.database.channel.email.EmailGlobalRepository;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionRepository;
import com.synopsys.integration.alert.web.channel.model.EmailDistributionConfig;

public class EmailChannelDescriptorTestIT extends DescriptorTestConfigTest<EmailDistributionConfig, EmailGroupDistributionConfigEntity, EmailGlobalConfigEntity> {

    @Autowired
    private EmailGlobalRepository emailGlobalRepository;

    @Autowired
    private EmailGroupDistributionRepository distributionRepository;

    @Autowired
    private EmailDescriptor emailDescriptor;

    @Override
    public void cleanGlobalRepository() {
        emailGlobalRepository.deleteAll();
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
    public void cleanDistributionRepository() {
        distributionRepository.deleteAll();
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
