package com.blackducksoftware.integration.hub.alert.channel.email;

import com.blackducksoftware.integration.hub.alert.channel.email.controller.distribution.EmailGroupDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.channel.email.mock.MockEmailEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.mock.MockEmailGlobalEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.mock.MockEmailRestModel;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.distribution.EmailGroupDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.manager.ChannelManagerTest;

public class EmailChannelManagerTest extends ChannelManagerTest<EmailGroupDistributionRestModel, EmailGroupDistributionConfigEntity, GlobalEmailConfigEntity> {

    @Override
    public String getDestination() {
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
