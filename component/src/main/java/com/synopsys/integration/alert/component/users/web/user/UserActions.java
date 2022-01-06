/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.users.web.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.action.api.AbstractResourceActions;
import com.synopsys.integration.alert.common.action.api.ActionMessageCreator;
import com.synopsys.integration.alert.common.descriptor.accessor.RoleAccessor;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DefaultUserRole;
import com.synopsys.integration.alert.common.logging.AlertLoggerFactory;
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
public class UserActions extends AbstractResourceActions<UserConfig, UserModel, MultiUserConfigResponseModel> {
    public static final String FIELD_KEY_USER_MGMT_USERNAME = "username";
    public static final String FIELD_KEY_USER_MGMT_PASSWORD = "password";
    public static final String FIELD_KEY_USER_MGMT_EMAILADDRESS = "emailAddress";
    public static final String FIELD_KEY_USER_MGMT_ROLES = "roleNames";

    private static final int DEFAULT_PASSWORD_LENGTH = 8;
    private final UserAccessor userAccessor;
    private final RoleAccessor roleAccessor;
    private final AuthorizationManager authorizationManager;
    private final AuthenticationTypeAccessor authenticationTypeAccessor;
    private final UserSystemValidator userSystemValidator;

    private final Logger logger = AlertLoggerFactory.getLogger(UserActions.class);
    private final ActionMessageCreator actionMessageCreator = new ActionMessageCreator();

    @Autowired
    public UserActions(UserManagementDescriptorKey userManagementDescriptorKey, UserAccessor userAccessor, RoleAccessor roleAccessor, AuthorizationManager authorizationManager,
        AuthenticationTypeAccessor authenticationTypeAccessor,
        UserSystemValidator userSystemValidator) {
        super(userManagementDescriptorKey, ConfigContextEnum.GLOBAL, authorizationManager);
        this.userAccessor = userAccessor;
        this.roleAccessor = roleAccessor;
        this.authorizationManager = authorizationManager;
        this.authenticationTypeAccessor = authenticationTypeAccessor;
        this.userSystemValidator = userSystemValidator;
    }

    @Override
    protected Optional<UserConfig> findExisting(Long id) {
        return userAccessor.getUser(id)
                   .map(this::convertDatabaseModelToRestModel);
    }

    @Override
    protected List<UserModel> retrieveDatabaseModels() {
        return userAccessor.getUsers();
    }

    @Override
    protected MultiUserConfigResponseModel createMultiResponseModel(List<UserConfig> users) {
        return new MultiUserConfigResponseModel(users);
    }

    @Override
    protected UserConfig convertDatabaseModelToRestModel(UserModel userModel) {
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

    @Override
    protected ValidationActionResponse testWithoutChecks(UserConfig resource) {
        return validateWithoutChecks(resource);
    }

    @Override
    protected ValidationActionResponse validateWithoutChecks(UserConfig resource) {
        ValidationResponseModel responseModel;
        if (StringUtils.isNotBlank(resource.getId()) && !NumberUtils.isCreatable(resource.getId())) {
            responseModel = ValidationResponseModel.generalError("Invalid resource id");
            return new ValidationActionResponse(HttpStatus.BAD_REQUEST, responseModel);
        }
        List<AlertFieldStatus> fieldErrors = validateCreationRequiredFields(resource);
        validateUserRole(resource).ifPresent(fieldErrors::add);
        if (fieldErrors.isEmpty()) {
            responseModel = ValidationResponseModel.success("The user is valid");
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

            logger.debug(actionMessageCreator.createStartMessage("user", userName));
            UserModel userModel = userAccessor.addUser(userName, password, emailAddress);
            Long userId = userModel.getId();
            Set<String> configuredRoleNames = resource.getRoleNames();
            if (null != configuredRoleNames && !configuredRoleNames.isEmpty()) {
                Collection<UserRoleModel> roleNames = roleAccessor.getRoles().stream()
                                                          .filter(role -> configuredRoleNames.contains(role.getName()))
                                                          .collect(Collectors.toList());
                authorizationManager.updateUserRoles(userId, roleNames);
            }
            userModel = userAccessor.getUser(userId).orElse(userModel);
            logger.debug(actionMessageCreator.createSuccessMessage("User", userName));
            return new ActionResponse<>(HttpStatus.CREATED, convertDatabaseModelToRestModel(userModel));
        } catch (AlertException ex) {
            logger.error(actionMessageCreator.createErrorMessage("user", resource.getUsername()));
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
                logger.debug(actionMessageCreator.updateStartMessage("user", userName));
                userAccessor.updateUser(newUserModel, passwordMissing);
                Set<String> configuredRoleNames = resource.getRoleNames();
                if (null != configuredRoleNames && !configuredRoleNames.isEmpty()) {
                    Collection<UserRoleModel> roleNames = roleAccessor.getRoles().stream()
                                                              .filter(role -> configuredRoleNames.contains(role.getName()))
                                                              .collect(Collectors.toList());
                    authorizationManager.updateUserRoles(existingUser.getId(), roleNames);
                }
                userSystemValidator.validateDefaultAdminUser(id);
                UserConfig user = userAccessor.getUser(id)
                                      .map(this::convertDatabaseModelToRestModel)
                                      .orElse(resource);
                logger.debug(actionMessageCreator.updateSuccessMessage("User", userName));
                return new ActionResponse<>(HttpStatus.NO_CONTENT);
            } catch (AlertException ex) {
                logger.error(actionMessageCreator.updateErrorMessage("User", userName));
                return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
            }
        }
        logger.warn(actionMessageCreator.updateNotFoundMessage("User", id));
        return new ActionResponse<>(HttpStatus.NOT_FOUND);
    }

    @Override
    protected ActionResponse<UserConfig> deleteWithoutChecks(Long id) {
        Optional<UserModel> user = userAccessor.getUser(id);
        if (user.isPresent()) {
            String userName = user.get().getName();
            try {
                logger.debug(actionMessageCreator.deleteStartMessage("user", userName));
                userAccessor.deleteUser(id);
            } catch (AlertException ex) {
                logger.error(actionMessageCreator.deleteErrorMessage("user", userName));
                return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
            }
            logger.debug(actionMessageCreator.deleteSuccessMessage("User", userName));
            return new ActionResponse<>(HttpStatus.NO_CONTENT);
        }
        logger.warn(actionMessageCreator.deleteNotFoundMessage("User", id));
        return new ActionResponse<>(HttpStatus.NOT_FOUND);
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

    private Optional<AlertFieldStatus> validateUserRole(UserConfig userConfig) {
        String id = userConfig.getId();
        if (StringUtils.isBlank(id)) {
            return Optional.empty();
        }

        long userId = Long.parseLong(id);
        if (UserAccessor.DEFAULT_ADMIN_USER_ID == userId) {
            return ensureUserHasRole(userConfig.getRoleNames(), DefaultUserRole.ALERT_ADMIN);
        }
        return Optional.empty();
    }

    private Optional<AlertFieldStatus> ensureUserHasRole(Set<String> roleNames, DefaultUserRole defaultUserRole) {
        String roleName = defaultUserRole.name();
        if (!roleNames.contains(roleName)) {
            return Optional.of(AlertFieldStatus.error(FIELD_KEY_USER_MGMT_ROLES, "User must have role: " + roleName));
        }
        return Optional.empty();
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
