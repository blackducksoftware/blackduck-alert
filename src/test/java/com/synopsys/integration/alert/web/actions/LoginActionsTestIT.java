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

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.TestProperties;
import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.database.api.user.UserAccessor;
import com.synopsys.integration.alert.database.user.UserEntity;
import com.synopsys.integration.alert.database.user.UserRepository;
import com.synopsys.integration.alert.mock.model.MockLoginRestModel;
import com.synopsys.integration.test.annotation.HubConnectionTest;

@Category(HubConnectionTest.class)
public class LoginActionsTestIT extends AlertIntegrationTest {
    private final Logger logger = LoggerFactory.getLogger(LoginActionsTestIT.class);
    private final MockLoginRestModel mockLoginRestModel = new MockLoginRestModel();
    private final TestProperties properties = new TestProperties();
    @Autowired
    private DaoAuthenticationProvider alertDatabaseAuthProvider;
    @Autowired
    private UserAccessor userAccessor;

    @Autowired
    private UserRepository userRepository;

    @Before
    public void init() throws IOException {

        mockLoginRestModel.setBlackDuckUsername(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_USERNAME));
        mockLoginRestModel.setBlackDuckPassword(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_PASSWORD));
    }

    @Test
    public void authenticateUserTestIT() {
        final LoginActions loginActions = new LoginActions(alertDatabaseAuthProvider);
        final boolean userAuthenticated = loginActions.authenticateUser(mockLoginRestModel.createRestModel());

        assertTrue(userAuthenticated);
    }

    @Test
    public void testAuthenticateUserFailIT() throws IOException {
        mockLoginRestModel.setBlackDuckUsername(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_ACTIVE_USER));
        final LoginActions loginActions = new LoginActions(alertDatabaseAuthProvider);
        final MockLoginRestModel badRestModel = new MockLoginRestModel();
        badRestModel.setBlackDuckPassword("badpassword");
        try {
            loginActions.authenticateUser(badRestModel.createRestModel());
            fail();
        } catch (final BadCredentialsException ex) {

        }
    }

    @Test
    public void testAuthenticateUserRoleFailIT() throws IOException {
        // add a user test then delete a user.
        final String userName = properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_ACTIVE_USER);
        mockLoginRestModel.setBlackDuckUsername(userName);
        final LoginActions loginActions = new LoginActions(alertDatabaseAuthProvider);
        userAccessor.addUser(userName, mockLoginRestModel.getBlackDuckPassword());
        final boolean userAuthenticated = loginActions.authenticateUser(mockLoginRestModel.createRestModel());

        assertFalse(userAuthenticated);

        final Optional<UserEntity> userEntity = userRepository.findByUserName(userName);
        userEntity.ifPresent(entity -> {
            userAccessor.assignRoles(entity.getUserName(), Collections.emptySet());
            userRepository.delete(entity);
        });
    }
}
