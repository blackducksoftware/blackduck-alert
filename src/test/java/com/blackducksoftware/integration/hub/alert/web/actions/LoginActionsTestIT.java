/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.alert.web.actions;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.channel.RestChannelTest;
import com.blackducksoftware.integration.hub.alert.web.model.LoginRestModel;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

public class LoginActionsTestIT extends RestChannelTest {
    private final Logger logger = LoggerFactory.getLogger(LoginActionsTestIT.class);

    @Test
    public void authenticateUserTestIT() throws IntegrationException {
        Assume.assumeTrue(properties.containsKey("blackduck.hub.url"));
        Assume.assumeTrue(properties.containsKey("blackduck.hub.timeout"));
        Assume.assumeTrue(properties.containsKey("blackduck.hub.username"));
        Assume.assumeTrue(properties.containsKey("blackduck.hub.password"));
        Assume.assumeTrue(properties.containsKey("blackduck.hub.always.trust.cert"));

        final LoginActions loginActions = new LoginActions();
        final LoginRestModel loginRestModel = new LoginRestModel("", properties.getProperty("blackduck.hub.url"), properties.getProperty("blackduck.hub.timeout"), properties.getProperty("blackduck.hub.username"),
                properties.getProperty("blackduck.hub.password"), "", "", "", "", properties.getProperty("blackduck.hub.always.trust.cert"));
        final boolean userAuthenticated = loginActions.authenticateUser(loginRestModel, new Slf4jIntLogger(logger));

        Assert.assertTrue(userAuthenticated);
    }
}
