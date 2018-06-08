package com.blackducksoftware.integration.hub.alert.channel.email;

import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.channel.email.controller.distribution.EmailGroupDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.channel.email.mock.MockEmailEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.mock.MockEmailGlobalEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.mock.MockEmailRestModel;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.distribution.EmailGroupDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.distribution.EmailGroupDistributionRepository;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailRepository;
import com.blackducksoftware.integration.hub.alert.channel.manager.ChannelManagerTest;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;

public class EmailChannelManagerTest extends ChannelManagerTest<EmailGroupDistributionRestModel, EmailGroupDistributionConfigEntity, GlobalEmailConfigEntity, EmailGroupManager> {

    @Override
    public EmailGroupManager getChannelManager() {
        final EmailGroupChannel mockEmailChannel = Mockito.mock(EmailGroupChannel.class);
        final GlobalEmailRepository mockGlobalRepositoryWrapper = Mockito.mock(GlobalEmailRepository.class);
        final EmailGroupDistributionRepository mockRepositoryWrapper = Mockito.mock(EmailGroupDistributionRepository.class);
        final EmailGroupManager manager = new EmailGroupManager(mockEmailChannel, mockGlobalRepositoryWrapper, mockRepositoryWrapper, new ObjectTransformer(), contentConverter);

        return manager;
    }

    @Override
    public String getSupportedChannelName() {
        return EmailGroupChannel.COMPONENT_NAME;
    }

    @Override
    public String channelTopic() {
        return EmailGroupChannel.COMPONENT_NAME;
    }

    @Override
    public MockEmailEntity getMockEntityUtil() {
        return new MockEmailEntity();
    }

    @Override
    public MockEmailGlobalEntity getMockGlobalEntityUtil() {
        return new MockEmailGlobalEntity();
    }

    @Override
    public MockEmailRestModel getMockRestModelUtil() {
        return new MockEmailRestModel();
    }

}
