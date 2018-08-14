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
package com.synopsys.integration.alert.web.actions;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.OutputLogger;
import com.synopsys.integration.alert.TestAlertProperties;
import com.synopsys.integration.alert.TestBlackDuckProperties;
import com.synopsys.integration.alert.TestProperties;
import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.mock.model.MockLoginRestModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.web.model.LoginConfig;
import com.synopsys.integration.blackduck.configuration.HubServerConfigBuilder;
import com.synopsys.integration.blackduck.rest.BlackduckRestConnection;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.test.annotation.HubConnectionTest;

@Category(HubConnectionTest.class)
public class LoginActionsTestIT {
    private final Logger logger = LoggerFactory.getLogger(LoginActionsTestIT.class);
    private final MockLoginRestModel mockLoginRestModel = new MockLoginRestModel();
    private final TestProperties properties = new TestProperties();

    private OutputLogger outputLogger;

    @Before
    public void init() throws IOException {
        outputLogger = new OutputLogger();

        mockLoginRestModel.setBlackDuckUsername(properties.getProperty(TestPropertyKey.TEST_USERNAME));
        mockLoginRestModel.setBlackDuckPassword(properties.getProperty(TestPropertyKey.TEST_PASSWORD));
    }

    @After
    public void cleanup() throws IOException {
        outputLogger.cleanup();
    }

    @Test
    public void authenticateUserTestIT() throws IntegrationException {
        final LoginActions loginActions = new LoginActions(new TestBlackDuckProperties(new TestAlertProperties()));
        final boolean userAuthenticated = loginActions.authenticateUser(mockLoginRestModel.createRestModel(), new Slf4jIntLogger(logger));

        Assert.assertTrue(userAuthenticated);
    }

    @Test
    public void testAuthenticateUserFailIT() throws IntegrationException, IOException {
        mockLoginRestModel.setBlackDuckUsername(properties.getProperty(TestPropertyKey.TEST_ACTIVE_USER));
        final LoginActions loginActions = new LoginActions(new TestBlackDuckProperties(new TestAlertProperties()));
        final MockLoginRestModel badRestModel = new MockLoginRestModel();
        badRestModel.setBlackDuckPassword("badpassword");
        final boolean userAuthenticated = loginActions.authenticateUser(badRestModel.createRestModel(), new Slf4jIntLogger(logger));

        assertFalse(userAuthenticated);
        assertTrue(outputLogger.isLineContainingText("User not authenticated"));
    }

    @Test
    public void testAuthenticateUserRoleFailIT() throws IntegrationException, IOException {
        mockLoginRestModel.setBlackDuckUsername(properties.getProperty(TestPropertyKey.TEST_ACTIVE_USER));
        final LoginActions loginActions = new LoginActions(new TestBlackDuckProperties(new TestAlertProperties()));

        final boolean userAuthenticated = loginActions.authenticateUser(mockLoginRestModel.createRestModel(), new Slf4jIntLogger(logger));

        assertFalse(userAuthenticated);
        assertTrue(outputLogger.isLineContainingText("User role not authenticated"));
    }

    @Test
    public void testIsUserValidFailIT() throws IntegrationException {
        final LoginConfig loginConfig = mockLoginRestModel.createRestModel();
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final BlackDuckProperties blackDuckProperties = new TestBlackDuckProperties(testAlertProperties);
        final HubServerConfigBuilder serverConfigBuilder = new HubServerConfigBuilder();
        serverConfigBuilder.setLogger(new Slf4jIntLogger(logger));
        serverConfigBuilder.setUrl(blackDuckProperties.getBlackDuckUrl().orElse(null));
        serverConfigBuilder.setTrustCert(testAlertProperties.getAlertTrustCertificate().orElse(false));
        serverConfigBuilder.setTimeout(blackDuckProperties.getBlackDuckTimeout());
        serverConfigBuilder.setPassword(loginConfig.getBlackDuckPassword());
        serverConfigBuilder.setUsername(loginConfig.getBlackDuckUsername());

        final LoginActions loginActions = new LoginActions(blackDuckProperties);

        final BlackduckRestConnection restConnection = loginActions.createRestConnection(serverConfigBuilder);

        final boolean roleValid = loginActions.isUserRoleValid("broken", restConnection);

        assertFalse(roleValid);
    }

    @Test
    public void testValidateHubConfigurationException() {
        mockLoginRestModel.setBlackDuckUsername(null);
        final LoginActions loginActions = new LoginActions(new TestBlackDuckProperties(new TestAlertProperties()));

        try {
            final boolean authenticated = loginActions.authenticateUser(mockLoginRestModel.createRestModel(), new Slf4jIntLogger(logger));
            assertFalse(authenticated);
        } catch (final IntegrationException e) {
            fail();
        }
    }
}
