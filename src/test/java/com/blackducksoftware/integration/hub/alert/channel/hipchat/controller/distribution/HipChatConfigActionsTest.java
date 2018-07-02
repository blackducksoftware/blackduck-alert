package com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.distribution;

import com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.distribution.HipChatDistributionConfigActions;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.distribution.HipChatDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.mock.MockHipChatEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.mock.MockHipChatRestModel;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.distribution.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockEntityUtil;
import com.blackducksoftware.integration.hub.alert.mock.model.MockRestModelUtil;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.SimpleDistributionConfigActions;
import com.blackducksoftware.integration.hub.alert.web.actions.SimpleDistributionConfigActionsTest;

public class HipChatConfigActionsTest extends SimpleDistributionConfigActionsTest<HipChatDistributionConfigEntity, HipChatDistributionRestModel> {

    @Override
    public SimpleDistributionConfigActions<HipChatDistributionConfigEntity, HipChatDistributionRestModel> getConfigActions() {
        return new HipChatDistributionConfigActions(new ObjectTransformer());
    }

    @Override
    public MockRestModelUtil<HipChatDistributionRestModel> getMockRestModelUtil() {
        return new MockHipChatRestModel();
    }

    @Override
    public int getRequiredFieldErrorCount() {
        return 1;
    }

    @Override
    public MockEntityUtil<HipChatDistributionConfigEntity> getMockEntityUtil() {
        return new MockHipChatEntity();
    }

}
