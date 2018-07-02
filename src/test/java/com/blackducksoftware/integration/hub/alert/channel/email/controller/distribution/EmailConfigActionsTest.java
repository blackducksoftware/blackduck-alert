package com.blackducksoftware.integration.hub.alert.channel.email.controller.distribution;

import com.blackducksoftware.integration.hub.alert.channel.email.controller.distribution.EmailGroupDistributionConfigActions;
import com.blackducksoftware.integration.hub.alert.channel.email.controller.distribution.EmailGroupDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.channel.email.mock.MockEmailEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.mock.MockEmailRestModel;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.distribution.EmailGroupDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockEntityUtil;
import com.blackducksoftware.integration.hub.alert.mock.model.MockRestModelUtil;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.SimpleDistributionConfigActions;
import com.blackducksoftware.integration.hub.alert.web.actions.SimpleDistributionConfigActionsTest;

public class EmailConfigActionsTest extends SimpleDistributionConfigActionsTest<EmailGroupDistributionConfigEntity, EmailGroupDistributionRestModel> {

    @Override
    public SimpleDistributionConfigActions<EmailGroupDistributionConfigEntity, EmailGroupDistributionRestModel> getConfigActions() {
        return new EmailGroupDistributionConfigActions(new ObjectTransformer());
    }

    @Override
    public MockRestModelUtil<EmailGroupDistributionRestModel> getMockRestModelUtil() {
        return new MockEmailRestModel();
    }

    @Override
    public int getRequiredFieldErrorCount() {
        return 1;
    }

    @Override
    public MockEntityUtil<EmailGroupDistributionConfigEntity> getMockEntityUtil() {
        return new MockEmailEntity();
    }

}
