/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.api;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.common.model.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.common.exception.AlertForbiddenOperationException;
import com.synopsys.integration.alert.common.persistence.accessor.AuthenticationTypeAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.AuthenticationTypeDetails;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.database.user.UserEntity;
import com.synopsys.integration.alert.database.user.UserRepository;
import com.synopsys.integration.alert.database.user.UserRoleRelation;
import com.synopsys.integration.alert.database.user.UserRoleRepository;

@Component
public class DefaultUserAccessor implements UserAccessor {
    private static final Set<Long> RESERVED_USER_IDS = Set.of(UserAccessor.DEFAULT_ADMIN_USER_ID, UserAccessor.DEFAULT_JOB_MANAGER_ID, UserAccessor.DEFAULT_ALERT_USER_ID);

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder defaultPasswordEncoder;
    private final DefaultRoleAccessor roleAccessor;
    private final AuthenticationTypeAccessor authenticationTypeAccessor;

    @Autowired
    public DefaultUserAccessor(UserRepository userRepository, UserRoleRepository userRoleRepository, PasswordEncoder defaultPasswordEncoder, DefaultRoleAccessor roleAccessor,
        AuthenticationTypeAccessor authenticationTypeAccessor) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.defaultPasswordEncoder = defaultPasswordEncoder;
        this.roleAccessor = roleAccessor;
        this.authenticationTypeAccessor = authenticationTypeAccessor;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserModel> getUsers() {
        List<UserEntity> userList = userRepository.findAll();
        return userList.stream().map(this::createModel).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserModel> getUser(Long userId) {
        return userRepository.findById(userId).map(this::createModel);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserModel> getUser(String username) {
        return userRepository.findByUserName(username).map(this::createModel);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public UserModel addUser(String userName, String password, String emailAddress) throws AlertConfigurationException {
        return addUser(UserModel.newUser(userName, password, emailAddress, AuthenticationType.DATABASE, Collections.emptySet(), true), false);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public UserModel addUser(UserModel user, boolean passwordEncoded) throws AlertConfigurationException {
        String username = user.getName();
        Optional<UserEntity> userWithSameUsername = userRepository.findByUserName(username);
        if (userWithSameUsername.isPresent()) {
            throw new AlertConfigurationException(String.format("A user with username '%s' is already present", username));
        }

        String password = passwordEncoded ? user.getPassword() : defaultPasswordEncoder.encode(user.getPassword());
        AuthenticationTypeDetails authenticationType = authenticationTypeAccessor.getAuthenticationTypeDetails(user.getAuthenticationType())
                                                           .orElseThrow(() -> new AlertRuntimeException("Cannot find Authentication Type."));
        UserEntity newEntity = new UserEntity(username, password, user.getEmailAddress(), authenticationType.getId());
        UserEntity savedEntity = userRepository.save(newEntity);
        UserModel model = createModel(savedEntity);

        roleAccessor.updateUserRoles(model.getId(), user.getRoles());

        return model;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public UserModel updateUser(UserModel user, boolean passwordEncoded) throws AlertConfigurationException, AlertForbiddenOperationException {
        Long userId = user.getId();
        UserEntity existingUser = userRepository.findById(userId)
                                      .orElseThrow(() -> new AlertConfigurationException(String.format("No user found with id '%s'", userId)));
        Long existingUserId = existingUser.getId();
        UserEntity savedEntity = existingUser;
        // if it isn't an external user then update username, password, and email.
        Optional<AuthenticationType> authenticationType = authenticationTypeAccessor.getAuthenticationType(existingUser.getAuthenticationType());
        if (authenticationType.isEmpty()) {
            throw new AlertRuntimeException("Unknown Authentication Type, user not updated.");
        } else if (AuthenticationType.DATABASE != authenticationType.get()) {
            boolean isUserNameInvalid = !StringUtils.equals(existingUser.getUserName(), user.getName());
            boolean isEmailInvalid = !StringUtils.equals(existingUser.getEmailAddress(), user.getEmailAddress());
            boolean isPasswordSet = StringUtils.isNotBlank(user.getPassword());
            if (isUserNameInvalid || isEmailInvalid || isPasswordSet) {
                throw new AlertForbiddenOperationException("An external user cannot change its credentials.");
            }
        } else {
            String password = passwordEncoded ? user.getPassword() : defaultPasswordEncoder.encode(user.getPassword());
            UserEntity newEntity = new UserEntity(user.getName(), password, user.getEmailAddress(), user.isExpired(), user.isLocked(), user.isPasswordExpired(), user.isEnabled(), existingUser.getAuthenticationType());
            newEntity.setId(existingUserId);
            savedEntity = userRepository.save(newEntity);
        }

        roleAccessor.updateUserRoles(existingUserId, user.getRoles());

        return createModel(savedEntity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean assignRoles(String username, Set<Long> roleIds) {
        Optional<Long> optionalUserId = userRepository.findByUserName(username).map(UserEntity::getId);
        if (optionalUserId.isPresent()) {
            roleAccessor.updateUserRoles(optionalUserId.get(), roleAccessor.getRoles(roleIds));
            return true;
        }
        return false;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean changeUserPassword(String username, String newPassword) {
        Optional<UserEntity> entity = userRepository.findByUserName(username);
        if (entity.isPresent()) {
            changeUserPassword(entity.get(), newPassword);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean changeUserEmailAddress(String username, String emailAddress) {
        Optional<UserEntity> entity = userRepository.findByUserName(username);
        if (entity.isPresent()) {
            changeUserEmailAddress(entity.get(), emailAddress);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteUser(String userName) throws AlertForbiddenOperationException {
        Optional<UserEntity> optionalUser = userRepository.findByUserName(userName);
        if (optionalUser.isPresent()) {
            UserEntity user = optionalUser.get();
            deleteUserEntity(user);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteUser(Long userId) throws AlertForbiddenOperationException {
        Optional<UserEntity> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            UserEntity user = optionalUser.get();
            deleteUserEntity(user);
        }
    }

    private void changeUserPassword(UserEntity oldEntity, String newPassword) {
        UserEntity updatedEntity = new UserEntity(oldEntity.getUserName(), defaultPasswordEncoder.encode(newPassword), oldEntity.getEmailAddress(), oldEntity.getAuthenticationType());
        updatedEntity.setId(oldEntity.getId());
        userRepository.save(updatedEntity);
    }

    private void changeUserEmailAddress(UserEntity oldEntity, String emailAddress) {
        UserEntity updatedEntity = new UserEntity(oldEntity.getUserName(), oldEntity.getPassword(), emailAddress, oldEntity.getAuthenticationType());
        updatedEntity.setId(oldEntity.getId());
        userRepository.save(updatedEntity);
    }

    private void deleteUserEntity(UserEntity userEntity) throws AlertForbiddenOperationException {
        Long userId = userEntity.getId();
        if (!RESERVED_USER_IDS.contains(userId)) {
            roleAccessor.updateUserRoles(userId, Set.of());
            userRepository.deleteById(userId);
        } else {
            String userIdentifier = userRepository.findById(userId).map(UserEntity::getUserName).orElse(String.valueOf(userId));
            throw new AlertForbiddenOperationException(String.format("The '%s' user is reserved and cannot be deleted.", userIdentifier));
        }
    }

    private UserModel createModel(UserEntity user) {
        List<UserRoleRelation> roleRelations = userRoleRepository.findAllByUserId(user.getId());
        List<Long> roleIdsForUser = roleRelations.stream().map(UserRoleRelation::getRoleId).collect(Collectors.toList());
        Set<UserRoleModel> roles = roleAccessor.getRoles(roleIdsForUser);
        AuthenticationType authenticationType = authenticationTypeAccessor.getAuthenticationType(user.getAuthenticationType()).orElse(null);
        return UserModel.existingUser(user.getId(), user.getUserName(), user.getPassword(), user.getEmailAddress(), authenticationType, roles, user.isEnabled());
    }

}
