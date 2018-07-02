package com.blackducksoftware.integration.hub.alert.channel.email.controller.global;

import com.blackducksoftware.integration.hub.alert.channel.email.controller.global.GlobalEmailConfigActions;
import com.blackducksoftware.integration.hub.alert.channel.email.controller.global.GlobalEmailConfigRestModel;
import com.blackducksoftware.integration.hub.alert.channel.email.mock.MockEmailGlobalRestModel;
import com.blackducksoftware.integration.hub.alert.web.actions.SimpleConfigActions;
import com.blackducksoftware.integration.hub.alert.web.actions.SimpleConfigActionsTest;

public class GlobalEmailConfigActionsTest extends SimpleConfigActionsTest<GlobalEmailConfigRestModel> {

    @Override
    public SimpleConfigActions<GlobalEmailConfigRestModel> getConfigActions() {
        return new GlobalEmailConfigActions();
    }

    @Override
    public MockEmailGlobalRestModel getMockRestModelUtil() {
        return new MockEmailGlobalRestModel();
    }

    @Override
    public int getRequiredFieldErrorCount() {
        return 0;
    }

}
