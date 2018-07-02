package com.blackducksoftware.integration.hub.alert.channel.slack.controller.distribution;

import com.blackducksoftware.integration.hub.alert.channel.slack.controller.distribution.SlackDistributionConfigActions;
import com.blackducksoftware.integration.hub.alert.channel.slack.controller.distribution.SlackDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.channel.slack.mock.MockSlackEntity;
import com.blackducksoftware.integration.hub.alert.channel.slack.mock.MockSlackRestModel;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.distribution.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockEntityUtil;
import com.blackducksoftware.integration.hub.alert.mock.model.MockRestModelUtil;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.SimpleDistributionConfigActions;
import com.blackducksoftware.integration.hub.alert.web.actions.SimpleDistributionConfigActionsTest;

public class SlackConfigActionsTest extends SimpleDistributionConfigActionsTest<SlackDistributionConfigEntity, SlackDistributionRestModel> {

    @Override
    public SimpleDistributionConfigActions<SlackDistributionConfigEntity, SlackDistributionRestModel> getConfigActions() {
        return new SlackDistributionConfigActions(new ObjectTransformer());
    }

    @Override
    public MockRestModelUtil<SlackDistributionRestModel> getMockRestModelUtil() {
        return new MockSlackRestModel();
    }

    @Override
    public int getRequiredFieldErrorCount() {
        return 2;
    }

    @Override
    public MockEntityUtil<SlackDistributionConfigEntity> getMockEntityUtil() {
        return new MockSlackEntity();
    }

}
