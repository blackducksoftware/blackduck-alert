package com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.global;

import com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.global.GlobalHipChatConfigActions;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.global.GlobalHipChatConfigRestModel;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.mock.MockHipChatGlobalRestModel;
import com.blackducksoftware.integration.hub.alert.mock.model.global.MockGlobalRestModelUtil;
import com.blackducksoftware.integration.hub.alert.web.actions.SimpleConfigActions;
import com.blackducksoftware.integration.hub.alert.web.actions.SimpleConfigActionsTest;

public class GlobalHipChatConfigActionsTest extends SimpleConfigActionsTest<GlobalHipChatConfigRestModel> {

    @Override
    public SimpleConfigActions<GlobalHipChatConfigRestModel> getConfigActions() {
        return new GlobalHipChatConfigActions();
    }

    @Override
    public MockGlobalRestModelUtil<GlobalHipChatConfigRestModel> getMockRestModelUtil() {
        return new MockHipChatGlobalRestModel();
    }

    @Override
    public int getRequiredFieldErrorCount() {
        return 1;
    }

}
