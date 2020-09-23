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
package com.synopsys.integration.alert.web.api.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.action.api.AbstractResourceActions;
import com.synopsys.integration.alert.common.descriptor.accessor.AuthorizationUtility;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.AuthenticationTypeAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.AuthenticationTypeDetails;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.users.UserManagementDescriptorKey;
import com.synopsys.integration.alert.component.users.UserSystemValidator;

@Component
@Transactional
public class UserActions extends AbstractResourceActions<UserConfig> {
    public static final String FIELD_KEY_USER_MGMT_USERNAME = "username";
    public static final String FIELD_KEY_USER_MGMT_PASSWORD = "password";
    public static final String FIELD_KEY_USER_MGMT_EMAILADDRESS = "emailAddress";
    private static final int DEFAULT_PASSWORD_LENGTH = 8;
    private final UserAccessor userAccessor;
    private final AuthorizationUtility authorizationUtility;
    private final AuthorizationManager authorizationManager;
    private final AuthenticationTypeAccessor authenticationTypeAccessor;
    private final UserSystemValidator userSystemValidator;

    @Autowired
    public UserActions(UserManagementDescriptorKey userManagementDescriptorKey, UserAccessor userAccessor, AuthorizationUtility authorizationUtility, AuthorizationManager authorizationManager,
        AuthenticationTypeAccessor authenticationTypeAccessor,
        UserSystemValidator userSystemValidator) {
        super(userManagementDescriptorKey, ConfigContextEnum.GLOBAL, authorizationManager);
        this.userAccessor = userAccessor;
        this.authorizationUtility = authorizationUtility;
        this.authorizationManager = authorizationManager;
        this.authenticationTypeAccessor = authenticationTypeAccessor;
        this.userSystemValidator = userSystemValidator;
    }

    @Override
    protected Optional<UserConfig> findExisting(Long id) {
        return userAccessor.getUser(id)
                   .map(this::convertToCustomUserRoleModel);
    }

    @Override
    protected ActionResponse<List<UserConfig>> readAllWithoutChecks() {
        List<UserConfig> users = userAccessor.getUsers().stream()
                                     .map(this::convertToCustomUserRoleModel)
                                     .collect(Collectors.toList());
        return new ActionResponse<>(HttpStatus.OK, users);
    }

    @Override
    protected ActionResponse<UserConfig> readWithoutChecks(Long id) {
        Optional<UserConfig> user = findExisting(id);
        if (user.isPresent()) {
            return new ActionResponse<>(HttpStatus.OK, user.get());
        }
        return new ActionResponse<>(HttpStatus.NOT_FOUND, String.format("The user with id: %d not found.", id));
    }

    @Override
    protected ValidationActionResponse testWithoutChecks(UserConfig resource) {
        return validateWithoutChecks(resource);
    }

    @Override
    protected ValidationActionResponse validateWithoutChecks(UserConfig resource) {
        ValidationResponseModel responseModel;
        if (StringUtils.isNotBlank(resource.getId()) && !NumberUtils.isCreatable(resource.getId())) {
            responseModel = ValidationResponseModel.withoutFieldStatuses("Invalid resource id");
            return new ValidationActionResponse(HttpStatus.BAD_REQUEST, responseModel);
        }
        List<AlertFieldStatus> fieldErrors = validateCreationRequiredFields(resource);
        if (fieldErrors.isEmpty()) {
            responseModel = ValidationResponseModel.withoutFieldStatuses("The user is valid");
            return new ValidationActionResponse(HttpStatus.OK, responseModel);
        }
        responseModel = ValidationResponseModel.fromStatusCollection("There were problems validating this user.", fieldErrors);
        return new ValidationActionResponse(HttpStatus.BAD_REQUEST, responseModel);
    }

    @Override
    protected ActionResponse<UserConfig> createWithoutChecks(UserConfig resource) {
        try {
            String userName = resource.getUsername();
            String password = resource.getPassword();
            String emailAddress = resource.getEmailAddress();

            UserModel userModel = userAccessor.addUser(userName, password, emailAddress);
            Long userId = userModel.getId();
            Set<String> configuredRoleNames = resource.getRoleNames();
            if (null != configuredRoleNames && !configuredRoleNames.isEmpty()) {
                Collection<UserRoleModel> roleNames = authorizationUtility.getRoles().stream()
                                                          .filter(role -> configuredRoleNames.contains(role.getName()))
                                                          .collect(Collectors.toList());
                authorizationUtility.updateUserRoles(userId, roleNames);
            }
            userModel = userAccessor.getUser(userId).orElse(userModel);
            return new ActionResponse<>(HttpStatus.CREATED, convertToCustomUserRoleModel(userModel));
        } catch (AlertException ex) {
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, String.format("There was an issue creating the user. %s", ex.getMessage()));
        }
    }

    @Override
    protected ActionResponse<UserConfig> updateWithoutChecks(Long id, UserConfig resource) {
        Optional<UserModel> userModel = userAccessor.getUser(id);
        if (userModel.isPresent()) {
            UserModel existingUser = userModel.get();
            boolean passwordMissing = StringUtils.isBlank(resource.getPassword());
            String userName = resource.getUsername();
            String password = passwordMissing ? existingUser.getPassword() : resource.getPassword();
            String emailAddress = resource.getEmailAddress();

            UserModel newUserModel = UserModel.existingUser(existingUser.getId(), userName, password, emailAddress, existingUser.getAuthenticationType(), existingUser.getRoles(), existingUser.isEnabled());
            try {
                userAccessor.updateUser(newUserModel, passwordMissing);
                Set<String> configuredRoleNames = resource.getRoleNames();
                if (null != configuredRoleNames && !configuredRoleNames.isEmpty()) {
                    Collection<UserRoleModel> roleNames = authorizationUtility.getRoles().stream()
                                                              .filter(role -> configuredRoleNames.contains(role.getName()))
                                                              .collect(Collectors.toList());
                    authorizationUtility.updateUserRoles(existingUser.getId(), roleNames);
                    authorizationManager.loadPermissionsIntoCache();
                }
                userSystemValidator.validateDefaultAdminUser(id);
                UserConfig user = userAccessor.getUser(id)
                                      .map(this::convertToCustomUserRoleModel)
                                      .orElse(resource);
                return new ActionResponse<>(HttpStatus.OK);
            } catch (AlertException ex) {
                return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
            }
        }
        return new ActionResponse<>(HttpStatus.NOT_FOUND);
    }

    @Override
    protected ActionResponse<UserConfig> deleteWithoutChecks(Long id) {
        Optional<UserModel> user = userAccessor.getUser(id);
        if (user.isPresent()) {
            try {
                userAccessor.deleteUser(id);
            } catch (AlertException ex) {
                return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
            }
            return new ActionResponse<>(HttpStatus.NO_CONTENT);
        }
        return new ActionResponse<>(HttpStatus.NOT_FOUND);
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

    private List<AlertFieldStatus> validateCreationRequiredFields(UserConfig userConfig) {
        String userName = userConfig.getUsername();
        String password = userConfig.getPassword();
        String emailAddress = userConfig.getEmailAddress();

        List<AlertFieldStatus> fieldErrors = new ArrayList<>();

        if (userConfig.getId() == null) {
            validateUserExistsByName(fieldErrors, userName);
            validatePasswordLength(fieldErrors, password);
        } else {
            Long userId = Long.valueOf(userConfig.getId());
            validateUserExistsById(userId, userName).ifPresent(fieldErrors::add);
            Optional<UserModel> userModel = userAccessor.getUser(userId);
            if (userModel.isPresent()) {
                boolean passwordMissing = StringUtils.isBlank(userConfig.getPassword());

                if (!userConfig.isPasswordSet() || !passwordMissing) {
                    validatePasswordLength(fieldErrors, password);
                }
            }
        }
        if (!userConfig.isExternal()) {
            validateRequiredField(FIELD_KEY_USER_MGMT_EMAILADDRESS, fieldErrors, emailAddress);
        }

        return fieldErrors;
    }

    private void validateRequiredField(String fieldKey, List<AlertFieldStatus> fieldErrors, String fieldValue) {
        if (StringUtils.isBlank(fieldValue)) {
            fieldErrors.add(AlertFieldStatus.error(fieldKey, "This field is required."));
        }
    }

    private void validateUserExistsByName(List<AlertFieldStatus> fieldErrors, String userName) {
        validateRequiredField(FIELD_KEY_USER_MGMT_USERNAME, fieldErrors, userName);
        Optional<UserModel> userModel = userAccessor.getUser(userName);
        userModel.ifPresent(user -> fieldErrors.add(AlertFieldStatus.error(FIELD_KEY_USER_MGMT_USERNAME, "A user with that username already exists.")));
    }

    private Optional<AlertFieldStatus> validateUserExistsById(Long userId, String userName) {
        Optional<UserModel> userModel = userAccessor.getUser(userName);
        return userModel.filter(user -> !user.getId().equals(userId))
                   .map(user -> AlertFieldStatus.error(FIELD_KEY_USER_MGMT_USERNAME, "A user with that username already exists."));
    }

    private void validatePasswordLength(List<AlertFieldStatus> fieldErrors, String passwordValue) {
        validateRequiredField(FIELD_KEY_USER_MGMT_PASSWORD, fieldErrors, passwordValue);
        if (fieldErrors.isEmpty() && DEFAULT_PASSWORD_LENGTH > passwordValue.length()) {
            fieldErrors.add(AlertFieldStatus.error(FIELD_KEY_USER_MGMT_PASSWORD, "The password needs to be at least 8 characters long."));
        }
    }
}
