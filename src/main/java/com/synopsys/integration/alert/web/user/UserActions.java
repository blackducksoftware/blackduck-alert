/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.accessor.AuthorizationUtility;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.web.model.UserConfig;

@Component
@Transactional
public class UserActions {
    private static final String FIELD_KEY_USER_MGMT_USERNAME = "username";
    private static final String FIELD_KEY_USER_MGMT_PASSWORD = "password";
    private static final String FIELD_KEY_USER_MGMT_EMAILADDRESS = "emailAddress";
    private static final int DEFAULT_PASSWORD_LENGTH = 8;
    private UserAccessor userAccessor;
    private AuthorizationUtility authorizationUtility;

    public UserActions(UserAccessor userAccessor, AuthorizationUtility authorizationUtility) {
        this.userAccessor = userAccessor;
        this.authorizationUtility = authorizationUtility;
    }

    public Collection<UserConfig> getUsers() {
        return userAccessor.getUsers().stream()
                   .map(this::convertToCustomUserRoleModel)
                   .collect(Collectors.toList());
    }

    public UserConfig createUser(UserConfig userConfig) throws AlertDatabaseConstraintException, AlertFieldException {
        validateRequiredFields(userConfig);
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
        return convertToCustomUserRoleModel(userModel);
    }

    public UserConfig updateUser(UserConfig userConfig) throws AlertFieldException, AlertDatabaseConstraintException {
        Long userId = Long.valueOf(userConfig.getId());
        Optional<UserModel> userModel = userAccessor.getUser(userId);
        if (userModel.isPresent()) {
            UserModel existingUser = userModel.get();
            String userName = userConfig.getUsername();
            String password = userConfig.getPassword();
            String emailAddress = userConfig.getEmailAddress();
            boolean passwordHasValue = StringUtils.isNotBlank(password);
            Map<String, String> fieldErrors = new HashMap<>();
            validateUserName(fieldErrors, userName);
            validateRequiredField(FIELD_KEY_USER_MGMT_EMAILADDRESS, fieldErrors, emailAddress);
            if (!userConfig.isPasswordSet() || passwordHasValue) {
                validatePasswordLength(fieldErrors, password);
            }
            if (!fieldErrors.isEmpty()) {
                throw new AlertFieldException(fieldErrors);
            }

            if (passwordHasValue) {
                UserModel newUserModel = UserModel.existingUser(existingUser.getId(), userName, password, emailAddress, existingUser.getRoles());
                userAccessor.updateUser(newUserModel, false);
            } else {
                UserModel newUserModel = UserModel.existingUser(existingUser.getId(), userName, existingUser.getPassword(), emailAddress, existingUser.getRoles());
                userAccessor.updateUser(newUserModel, true);
            }

            Set<String> configuredRoleNames = userConfig.getRoleNames();
            if (null != configuredRoleNames && !configuredRoleNames.isEmpty() && userModel.isPresent()) {
                Collection<UserRoleModel> roleNames = authorizationUtility.getRoles().stream()
                                                          .filter(role -> configuredRoleNames.contains(role.getName()))
                                                          .collect(Collectors.toList());
                authorizationUtility.updateUserRoles(userModel.get().getId(), roleNames);
            }
        }
        return userAccessor.getUser(userId)
                   .map(this::convertToCustomUserRoleModel)
                   .orElse(userConfig);
    }

    public void deleteUser(Long userId) throws AlertDatabaseConstraintException {
        userAccessor.deleteUser(userId);
    }

    private UserConfig convertToCustomUserRoleModel(UserModel userModel) {
        // converting to an object to return to the client; remove the password field.
        return new UserConfig(userModel.getId().toString(),
            userModel.getName(),
            null,
            userModel.getEmailAddress(),
            userModel.getRoleNames(),
            userModel.isExpired(),
            userModel.isLocked(),
            userModel.isPasswordExpired(),
            userModel.isEnabled(),
            StringUtils.isNotBlank(userModel.getPassword()));
    }

    private void validateRequiredFields(UserConfig userConfig) throws AlertFieldException {
        String userName = userConfig.getUsername();
        String password = userConfig.getPassword();
        String emailAddress = userConfig.getEmailAddress();

        Map<String, String> fieldErrors = new HashMap<>();
        validateUserName(fieldErrors, userName);
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

    private void validateUserName(Map<String, String> fieldErrors, String userName) {
        validateRequiredField(FIELD_KEY_USER_MGMT_USERNAME, fieldErrors, userName);
        Optional<UserModel> userModel = userAccessor.getUser(userName);
        userModel.ifPresent(user -> fieldErrors.put(FIELD_KEY_USER_MGMT_USERNAME, "A user with that username already exists."));
    }

    private void validatePasswordLength(Map<String, String> fieldErrors, String passwordValue) {
        validateRequiredField(FIELD_KEY_USER_MGMT_PASSWORD, fieldErrors, passwordValue);
        if (fieldErrors.isEmpty()) {
            if (DEFAULT_PASSWORD_LENGTH > passwordValue.length()) {
                fieldErrors.put(FIELD_KEY_USER_MGMT_PASSWORD, "The password need to be at least 8 characters long.");
            }
        }
    }
}
