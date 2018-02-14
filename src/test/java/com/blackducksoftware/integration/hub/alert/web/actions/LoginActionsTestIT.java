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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.OutputLogger;
import com.blackducksoftware.integration.hub.alert.TestProperties;
import com.blackducksoftware.integration.hub.alert.TestPropertyKey;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.mock.model.MockLoginRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.LoginRestModel;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

public class LoginActionsTestIT {
    private final Logger logger = LoggerFactory.getLogger(LoginActionsTestIT.class);
    private final MockLoginRestModel mockLoginRestModel = new MockLoginRestModel();
    private final TestProperties properties = new TestProperties();

    private OutputLogger outputLogger;

    @Before
    public void init() throws IOException {
        outputLogger = new OutputLogger();

        mockLoginRestModel.setHubUrl(properties.getProperty(TestPropertyKey.TEST_HUB_SERVER_URL));
        mockLoginRestModel.setHubTimeout(properties.getProperty(TestPropertyKey.TEST_HUB_TIMEOUT));
        mockLoginRestModel.setHubUsername(properties.getProperty(TestPropertyKey.TEST_USERNAME));
        mockLoginRestModel.setHubPassword(properties.getProperty(TestPropertyKey.TEST_PASSWORD));
        mockLoginRestModel.setHubAlwaysTrustCertificate(properties.getProperty(TestPropertyKey.TEST_TRUST_HTTPS_CERT));

        mockLoginRestModel.setHubProxyHost("");
        mockLoginRestModel.setHubProxyPassword("");
        mockLoginRestModel.setHubProxyPort("");
        mockLoginRestModel.setHubProxyUsername("");
    }

    @After
    public void cleanup() throws IOException {
        outputLogger.cleanup();
    }

    @Test
    public void authenticateUserTestIT() throws IntegrationException {
        final LoginActions loginActions = new LoginActions();
        final boolean userAuthenticated = loginActions.authenticateUser(mockLoginRestModel.createRestModel(), new Slf4jIntLogger(logger));

        Assert.assertTrue(userAuthenticated);
    }

    @Test
    public void testAuthenticateUserFailIT() throws IntegrationException, IOException {
        mockLoginRestModel.setHubUsername(properties.getProperty(TestPropertyKey.TEST_ACTIVE_USER));
        final LoginActions loginActions = new LoginActions();

        final boolean userAuthenticated = loginActions.authenticateUser(mockLoginRestModel.createRestModel(), new Slf4jIntLogger(logger));

        assertFalse(userAuthenticated);
        assertTrue(outputLogger.isLineContainingText("User role not authenticated"));
    }

    @Test
    public void testIsUserValidFailIT() throws IntegrationException, IOException {
        final LoginRestModel loginRestModel = mockLoginRestModel.createRestModel();

        final HubServerConfigBuilder serverConfigBuilder = new HubServerConfigBuilder();
        serverConfigBuilder.setLogger(new Slf4jIntLogger(logger));
        serverConfigBuilder.setHubUrl(loginRestModel.getHubUrl());
        serverConfigBuilder.setPassword(loginRestModel.getHubPassword());
        serverConfigBuilder.setUsername(loginRestModel.getHubUsername());
        serverConfigBuilder.setTimeout(loginRestModel.getHubTimeout());
        serverConfigBuilder.setAlwaysTrustServerCertificate(Boolean.valueOf(loginRestModel.getHubAlwaysTrustCertificate()));
        serverConfigBuilder.setProxyHost(loginRestModel.getHubProxyHost());
        serverConfigBuilder.setProxyPort(loginRestModel.getHubProxyPort());
        serverConfigBuilder.setProxyUsername(loginRestModel.getHubProxyUsername());
        serverConfigBuilder.setProxyPassword(loginRestModel.getHubProxyPassword());

        final LoginActions loginActions = new LoginActions();

        final RestConnection restConnection = loginActions.createRestConnection(serverConfigBuilder);

        final boolean roleValid = loginActions.isUserRoleValid("broken", restConnection);

        assertFalse(roleValid);
    }

    @Test
    public void testValidateHubConfigurationException() {
        mockLoginRestModel.setHubProxyHost("break it");
        final LoginActions loginActions = new LoginActions();

        try {
            loginActions.authenticateUser(mockLoginRestModel.createRestModel(), new Slf4jIntLogger(logger));
            fail();
        } catch (final IntegrationException e) {
            if (!(e instanceof AlertFieldException)) {
                fail();
            }
        }
    }

}
