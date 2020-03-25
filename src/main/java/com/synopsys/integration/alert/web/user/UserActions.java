/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.web.user;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.accessor.AuthorizationUtility;
import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.exception.AlertForbiddenOperationException;
import com.synopsys.integration.alert.common.persistence.accessor.AuthenticationTypeAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageUtility;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.AuthenticationTypeDetails;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptor;
import com.synopsys.integration.alert.web.model.UserConfig;

@Component
@Transactional
public class UserActions {
    private static final Logger logger = LoggerFactory.getLogger(UserActions.class);
    public static final String FIELD_KEY_USER_MGMT_USERNAME = "username";
    public static final String FIELD_KEY_USER_MGMT_PASSWORD = "password";
    public static final String FIELD_KEY_USER_MGMT_EMAILADDRESS = "emailAddress";
    private static final int DEFAULT_PASSWORD_LENGTH = 8;
    private UserAccessor userAccessor;
    private AuthorizationUtility authorizationUtility;
    private AuthorizationManager authorizationManager;
    private AuthenticationTypeAccessor authenticationTypeAccessor;
    private SystemMessageUtility systemMessageUtility;

    @Autowired
    public UserActions(UserAccessor userAccessor, AuthorizationUtility authorizationUtility, AuthorizationManager authorizationManager, AuthenticationTypeAccessor authenticationTypeAccessor,
        SystemMessageUtility systemMessageUtility) {
        this.userAccessor = userAccessor;
        this.authorizationUtility = authorizationUtility;
        this.authorizationManager = authorizationManager;
        this.authenticationTypeAccessor = authenticationTypeAccessor;
        this.systemMessageUtility = systemMessageUtility;
    }

    public Collection<UserConfig> getUsers() {
        return userAccessor.getUsers().stream()
                   .map(this::convertToCustomUserRoleModel)
                   .collect(Collectors.toList());
    }

    public UserConfig createUser(UserConfig userConfig) throws AlertDatabaseConstraintException, AlertFieldException {
        validateCreationRequiredFields(userConfig);
        String userName = userConfig.getUsername();
        String password = userConfig.getPassword();
        String emailAddress = userConfig.getEmailAddress();
        UserModel userModel = userAccessor.addUser(userName, password, emailAddress);
        Long userId = userModel.getId();
        Set<String> configuredRoleNames = userConfig.getRoleNames();
        if (null != configuredRoleNames && !configuredRoleNames.isEmpty()) {
            Collection<UserRoleModel> roleNames = authorizationUtility.getRoles().stream()
                                                      .filter(role -> configuredRoleNames.contains(role.getName()))
                                                      .collect(Collectors.toList());
            authorizationUtility.updateUserRoles(userId, roleNames);
        }
        userModel = userAccessor.getUser(userId).orElse(userModel);
        validateSysadminUser(userId);
        return convertToCustomUserRoleModel(userModel);
    }

    public UserConfig updateUser(Long userId, UserConfig userConfig) throws AlertFieldException, AlertDatabaseConstraintException {
        Optional<UserModel> userModel = userAccessor.getUser(userId);
        if (userModel.isPresent()) {
            UserModel existingUser = userModel.get();
            boolean passwordMissing = StringUtils.isBlank(userConfig.getPassword());
            String userName = userConfig.getUsername();
            String password = passwordMissing ? existingUser.getPassword() : userConfig.getPassword();
            String emailAddress = userConfig.getEmailAddress();

            Map<String, String> fieldErrors = new HashMap<>();

            validateUserExistsById(fieldErrors, userId, userName);
            if (!existingUser.isExternal()) {
                validateRequiredField(FIELD_KEY_USER_MGMT_EMAILADDRESS, fieldErrors, emailAddress);
            }
            if (!userConfig.isPasswordSet() || !passwordMissing) {
                validatePasswordLength(fieldErrors, password);
            }
            if (!fieldErrors.isEmpty()) {
                throw new AlertFieldException(fieldErrors);
            }
            UserModel newUserModel = UserModel.existingUser(existingUser.getId(), userName, password, emailAddress, existingUser.getAuthenticationType(), existingUser.getRoles(), existingUser.isEnabled());
            userAccessor.updateUser(newUserModel, passwordMissing);

            Set<String> configuredRoleNames = userConfig.getRoleNames();
            if (null != configuredRoleNames && !configuredRoleNames.isEmpty()) {
                Collection<UserRoleModel> roleNames = authorizationUtility.getRoles().stream()
                                                          .filter(role -> configuredRoleNames.contains(role.getName()))
                                                          .collect(Collectors.toList());
                authorizationUtility.updateUserRoles(existingUser.getId(), roleNames);
                authorizationManager.loadPermissionsIntoCache();
            }
        }
        validateSysadminUser(userId);
        return userAccessor.getUser(userId)
                   .map(this::convertToCustomUserRoleModel)
                   .orElse(userConfig);
    }

    public void deleteUser(Long userId) throws AlertForbiddenOperationException {
        userAccessor.deleteUser(userId);
    }

    private UserConfig convertToCustomUserRoleModel(UserModel userModel) {
        // converting to an object to return to the client; remove the password field.
        // also if the user is external the password is set
        boolean external = userModel.isExternal();
        boolean passwordSet = StringUtils.isNotBlank(userModel.getPassword()) || external;
        Optional<AuthenticationTypeDetails> authenticationType = authenticationTypeAccessor.getAuthenticationTypeDetails(userModel.getAuthenticationType());
        String authTypeName = authenticationType.map(AuthenticationTypeDetails::getName).orElse("UNKNOWN");
        return new UserConfig(
            userModel.getId().toString(),
            userModel.getName(),
            null,
            userModel.getEmailAddress(),
            userModel.getRoleNames(),
            userModel.isExpired(),
            userModel.isLocked(),
            userModel.isPasswordExpired(),
            userModel.isEnabled(),
            passwordSet,
            authTypeName,
            external);
    }

    private void validateCreationRequiredFields(UserConfig userConfig) throws AlertFieldException {
        String userName = userConfig.getUsername();
        String password = userConfig.getPassword();
        String emailAddress = userConfig.getEmailAddress();

        Map<String, String> fieldErrors = new HashMap<>();
        validateUserExistsByName(fieldErrors, userName);
        validatePasswordLength(fieldErrors, password);
        validateRequiredField(FIELD_KEY_USER_MGMT_EMAILADDRESS, fieldErrors, emailAddress);
        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
    }

    private void validateRequiredField(String fieldKey, Map<String, String> fieldErrors, String fieldValue) {
        if (StringUtils.isBlank(fieldValue)) {
            fieldErrors.put(fieldKey, "This field is required.");
        }
    }

    private void validateUserExistsByName(Map<String, String> fieldErrors, String userName) {
        validateRequiredField(FIELD_KEY_USER_MGMT_USERNAME, fieldErrors, userName);
        Optional<UserModel> userModel = userAccessor.getUser(userName);
        userModel.ifPresent(user -> fieldErrors.put(FIELD_KEY_USER_MGMT_USERNAME, "A user with that username already exists."));
    }

    private void validateUserExistsById(Map<String, String> fieldErrors, Long userId, String userName) {
        validateRequiredField(FIELD_KEY_USER_MGMT_USERNAME, fieldErrors, userName);
        Optional<UserModel> userModel = userAccessor.getUser(userName);
        userModel.filter(user -> !user.getId().equals(userId))
            .ifPresent(user -> fieldErrors.put(FIELD_KEY_USER_MGMT_USERNAME, "A user with that username already exists."));
    }

    private void validatePasswordLength(Map<String, String> fieldErrors, String passwordValue) {
        validateRequiredField(FIELD_KEY_USER_MGMT_PASSWORD, fieldErrors, passwordValue);
        if (fieldErrors.isEmpty()) {
            if (DEFAULT_PASSWORD_LENGTH > passwordValue.length()) {
                fieldErrors.put(FIELD_KEY_USER_MGMT_PASSWORD, "The password need to be at least 8 characters long.");
            }
        }
    }

    public void validateSysadminUser(Long userId) {
        if (userId != UserAccessor.DEFAULT_ADMIN_USER_ID) {
            return;
        }
        try {
            systemMessageUtility.removeSystemMessagesByType(SystemMessageType.DEFAULT_ADMIN_USER_ERROR);
            Optional<UserModel> userModel = userAccessor.getUser(UserAccessor.DEFAULT_ADMIN_USER_ID);
            boolean missingEmailAddress = userModel.map(UserModel::getEmailAddress).filter(StringUtils::isNotBlank).isEmpty();
            if (missingEmailAddress) {
                systemMessageUtility.addSystemMessage(SettingsDescriptor.FIELD_ERROR_DEFAULT_USER_EMAIL, SystemMessageSeverity.ERROR, SystemMessageType.DEFAULT_ADMIN_USER_ERROR);
                logger.error(SettingsDescriptor.FIELD_ERROR_DEFAULT_USER_EMAIL);
            }

            boolean missingPassword = userModel.map(UserModel::getPassword).filter(StringUtils::isNotBlank).isEmpty();
            if (missingPassword) {
                systemMessageUtility.addSystemMessage(SettingsDescriptor.FIELD_ERROR_DEFAULT_USER_PWD, SystemMessageSeverity.ERROR, SystemMessageType.DEFAULT_ADMIN_USER_ERROR);
                logger.error(SettingsDescriptor.FIELD_ERROR_DEFAULT_USER_PWD);
            }
        } catch (Exception e) {
            logger.error("There was an unexpected error when attempting to validate the default admin user.", e);
        }
    }
}
