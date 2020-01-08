/**
 * alert-database
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
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertForbiddenOperationException;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.database.user.UserEntity;
import com.synopsys.integration.alert.database.user.UserRepository;
import com.synopsys.integration.alert.database.user.UserRoleRelation;
import com.synopsys.integration.alert.database.user.UserRoleRepository;

@Component
@Transactional
public class DefaultUserAccessor implements UserAccessor {
    private static final Set<Long> RESERVED_USER_IDS = Set.of(UserAccessor.DEFAULT_ADMIN_USER_ID, UserAccessor.DEFAULT_JOB_MANAGER_ID, UserAccessor.DEFAULT_ALERT_USER_ID);
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder defaultPasswordEncoder;
    private final DefaultAuthorizationUtility authorizationUtility;

    @Autowired
    public DefaultUserAccessor(UserRepository userRepository, UserRoleRepository userRoleRepository, PasswordEncoder defaultPasswordEncoder, DefaultAuthorizationUtility authorizationUtility) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.defaultPasswordEncoder = defaultPasswordEncoder;
        this.authorizationUtility = authorizationUtility;
    }

    @Override
    public List<UserModel> getUsers() {
        List<UserEntity> userList = userRepository.findAll();
        return userList.stream().map(this::createModel).collect(Collectors.toList());
    }

    @Override
    public Optional<UserModel> getUser(Long userId) {
        return userRepository.findById(userId).map(this::createModel);
    }

    @Override
    public Optional<UserModel> getUser(String username) {
        return userRepository.findByUserName(username).map(this::createModel);
    }

    @Override
    public UserModel addUser(String userName, String password, String emailAddress) throws AlertDatabaseConstraintException {
        return addUser(UserModel.newUser(userName, password, emailAddress, false, Collections.emptySet()), false);
    }

    @Override
    public UserModel addUser(UserModel user, boolean passwordEncoded) throws AlertDatabaseConstraintException {
        return addUser(user, passwordEncoded, false);
    }

    @Override
    public UserModel addExternalUser(UserModel user) throws AlertDatabaseConstraintException {
        return addUser(user, true, true);
    }

    private UserModel addUser(UserModel user, boolean passwordEncoded, boolean external) throws AlertDatabaseConstraintException {
        String username = user.getName();
        Optional<UserEntity> userWithSameUsername = userRepository.findByUserName(username);
        if (userWithSameUsername.isPresent()) {
            throw new AlertDatabaseConstraintException(String.format("A user with username '%s' is already present", username));
        }

        String password = passwordEncoded ? user.getPassword() : defaultPasswordEncoder.encode(user.getPassword());
        UserEntity newEntity = new UserEntity(username, password, user.getEmailAddress(), external);
        UserEntity savedEntity = userRepository.save(newEntity);
        UserModel model = createModel(savedEntity);

        authorizationUtility.updateUserRoles(model.getId(), user.getRoles());

        return model;
    }

    @Override
    public UserModel updateUser(UserModel user, boolean passwordEncoded) throws AlertDatabaseConstraintException {
        Long userId = user.getId();
        if (null == userId) {
            throw new AlertDatabaseConstraintException("A user id must be specified");
        }

        UserEntity existingUser = userRepository.findById(userId)
                                      .orElseThrow(() -> new AlertDatabaseConstraintException(String.format("No user found with id '%s'", userId)));
        Long existingUserId = existingUser.getId();
        UserEntity savedEntity = existingUser;
        // if it isn't an external user then update username, password, and email.
        if (existingUser.isExternal()) {
            boolean isUserNameInvalid = !StringUtils.equals(existingUser.getUserName(), user.getName());
            boolean isEmailInvalid = !StringUtils.equals(existingUser.getEmailAddress(), user.getEmailAddress());
            boolean isPasswordSet = StringUtils.isNotBlank(user.getPassword());
            if (isUserNameInvalid || isEmailInvalid || isPasswordSet) {
                throw new AlertDatabaseConstraintException("An external user cannot change its credentials.");
            }
        } else {
            String password = passwordEncoded ? user.getPassword() : defaultPasswordEncoder.encode(user.getPassword());
            UserEntity newEntity = new UserEntity(user.getName(), password, user.getEmailAddress(), user.isExpired(), user.isLocked(), user.isPasswordExpired(), user.isEnabled(), existingUser.isExternal());
            newEntity.setId(existingUserId);
            savedEntity = userRepository.save(newEntity);
        }

        authorizationUtility.updateUserRoles(existingUserId, user.getRoles());

        return createModel(savedEntity);
    }

    @Override
    public boolean assignRoles(String username, Set<Long> roleIds) {
        Optional<Long> optionalUserId = userRepository.findByUserName(username).map(UserEntity::getId);
        if (optionalUserId.isPresent()) {
            authorizationUtility.updateUserRoles(optionalUserId.get(), authorizationUtility.getRoles(roleIds));
            return true;
        }
        return false;
    }

    @Override
    public boolean changeUserPassword(String username, String newPassword) {
        Optional<UserEntity> entity = userRepository.findByUserName(username);
        if (entity.isPresent()) {
            return changeUserPassword(entity.get(), newPassword);
        }
        return false;
    }

    @Override
    public boolean changeUserEmailAddress(String username, String emailAddress) {
        Optional<UserEntity> entity = userRepository.findByUserName(username);
        if (entity.isPresent()) {
            return changeUserEmailAddress(entity.get(), emailAddress);
        }
        return false;
    }

    @Override
    public void deleteUser(String userName) throws AlertForbiddenOperationException {
        Optional<UserEntity> optionalUser = userRepository.findByUserName(userName);
        if (optionalUser.isPresent()) {
            UserEntity user = optionalUser.get();
            deleteUserEntity(user);
        }
    }

    @Override
    public void deleteUser(Long userId) throws AlertForbiddenOperationException {
        Optional<UserEntity> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            UserEntity user = optionalUser.get();
            deleteUserEntity(user);
        }
    }

    private boolean changeUserPassword(UserEntity oldEntity, String newPassword) {
        UserEntity updatedEntity = new UserEntity(oldEntity.getUserName(), defaultPasswordEncoder.encode(newPassword), oldEntity.getEmailAddress(), oldEntity.isExternal());
        updatedEntity.setId(oldEntity.getId());
        return userRepository.save(updatedEntity) != null;
    }

    private boolean changeUserEmailAddress(UserEntity oldEntity, String emailAddress) {
        UserEntity updatedEntity = new UserEntity(oldEntity.getUserName(), oldEntity.getPassword(), emailAddress, oldEntity.isExternal());
        updatedEntity.setId(oldEntity.getId());
        return userRepository.save(updatedEntity) != null;
    }

    private void deleteUserEntity(UserEntity userEntity) throws AlertForbiddenOperationException {
        Long userId = userEntity.getId();
        if (!RESERVED_USER_IDS.contains(userId)) {
            authorizationUtility.updateUserRoles(userId, Set.of());
            userRepository.deleteById(userId);
        } else {
            String userIdentifier = userRepository.findById(userId).map(UserEntity::getUserName).orElse(String.valueOf(userId));
            throw new AlertForbiddenOperationException(String.format("The '%s' user cannot be deleted", userIdentifier));
        }
    }

    private UserModel createModel(UserEntity user) {
        List<UserRoleRelation> roleRelations = userRoleRepository.findAllByUserId(user.getId());
        List<Long> roleIdsForUser = roleRelations.stream().map(UserRoleRelation::getRoleId).collect(Collectors.toList());
        Set<UserRoleModel> roles = authorizationUtility.getRoles(roleIdsForUser);
        return UserModel.existingUser(user.getId(), user.getUserName(), user.getPassword(), user.getEmailAddress(), user.isExternal(), roles);
    }

}
