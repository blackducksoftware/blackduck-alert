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
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.HubConnectionTest;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.TestProperties;
import com.blackducksoftware.integration.hub.alert.TestPropertyKey;
import com.blackducksoftware.integration.hub.alert.web.model.LoginRestModel;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

@Category(HubConnectionTest.class)
public class LoginActionsTestIT {
    private final Logger logger = LoggerFactory.getLogger(LoginActionsTestIT.class);

    @Test
    public void authenticateUserTestIT() throws IntegrationException {
        final TestProperties properties = new TestProperties();

        final LoginActions loginActions = new LoginActions();
        final LoginRestModel loginRestModel = new LoginRestModel("", properties.getProperty(TestPropertyKey.TEST_HUB_SERVER_URL), properties.getProperty(TestPropertyKey.TEST_HUB_TIMEOUT),
                properties.getProperty(TestPropertyKey.TEST_USERNAME), properties.getProperty(TestPropertyKey.TEST_PASSWORD), "", "", "", "", properties.getProperty(TestPropertyKey.TEST_TRUST_HTTPS_CERT));
        final boolean userAuthenticated = loginActions.authenticateUser(loginRestModel, new Slf4jIntLogger(logger));

        Assert.assertTrue(userAuthenticated);
    }
}
