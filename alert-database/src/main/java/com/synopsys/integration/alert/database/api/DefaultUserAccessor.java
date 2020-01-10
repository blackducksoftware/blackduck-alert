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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    public static final String DEFAULT_ADMIN_USER = "sysadmin";
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder defaultPasswordEncoder;
    private final DefaultAuthorizationUtility authorizationUtility;

    @Autowired
    public DefaultUserAccessor(UserRepository userRepository, UserRoleRepository userRoleRepository, PasswordEncoder defaultPasswordEncoder,
        DefaultAuthorizationUtility authorizationUtility) {
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
    public Optional<UserModel> getUser(String username) {
        return userRepository.findByUserName(username).map(this::createModel);
    }

    private UserModel createModel(UserEntity user) {
        List<UserRoleRelation> roleRelations = userRoleRepository.findAllByUserId(user.getId());
        List<Long> roleIdsForUser = roleRelations.stream().map(UserRoleRelation::getRoleId).collect(Collectors.toList());
        Set<UserRoleModel> roles = authorizationUtility.getRoles(roleIdsForUser);
        return UserModel.of(user.getUserName(), user.getPassword(), user.getEmailAddress(), roles, user.isEnabled());
    }

    @Override
    public UserModel addOrUpdateUser(UserModel user) {
        return addOrUpdateUser(user, false);
    }

    @Override
    public UserModel addOrUpdateUser(UserModel user, boolean passwordEncoded) {
        String password = passwordEncoded ? user.getPassword() : defaultPasswordEncoder.encode(user.getPassword());
        UserEntity userEntity = new UserEntity(user.getName(), password, user.getEmailAddress());

        Optional<UserEntity> existingUser = userRepository.findByUserName(user.getName());
        Long userId = null;
        if (existingUser.isPresent()) {
            userId = existingUser.get().getId();
            userEntity.setId(userId);
        }

        authorizationUtility.updateUserRoles(userId, user.getRoles());

        return createModel(userRepository.save(userEntity));
    }

    @Override
    public UserModel addUser(String userName, String password, String emailAddress) {
        return addOrUpdateUser(UserModel.of(userName, password, emailAddress, Collections.emptySet(), true));
    }

    @Override
    public boolean assignRoles(String username, Set<Long> roleIds) {
        Optional<UserEntity> entity = userRepository.findByUserName(username);
        boolean assigned = false;
        if (entity.isPresent()) {
            UserEntity existingUser = entity.get();
            UserModel model = addOrUpdateUser(UserModel.of(existingUser.getUserName(), existingUser.getPassword(), existingUser.getEmailAddress(), authorizationUtility.getRoles(roleIds), existingUser.isEnabled()));
            assigned = model.getName().equals(username) && model.getRoles().size() == roleIds.size();
        }
        return assigned;
    }

    @Override
    public boolean changeUserPassword(String username, String newPassword) {
        Optional<UserEntity> entity = userRepository.findByUserName(username);
        if (entity.isPresent()) {
            UserEntity oldEntity = entity.get();
            UserEntity updatedEntity = new UserEntity(oldEntity.getUserName(), defaultPasswordEncoder.encode(newPassword), oldEntity.getEmailAddress());
            updatedEntity.setId(oldEntity.getId());
            return userRepository.save(updatedEntity) != null;
        }
        return false;
    }

    @Override
    public boolean changeUserEmailAddress(String username, String emailAddress) {
        Optional<UserEntity> entity = userRepository.findByUserName(username);
        if (entity.isPresent()) {
            UserEntity oldEntity = entity.get();
            UserEntity updatedEntity = new UserEntity(oldEntity.getUserName(), oldEntity.getPassword(), emailAddress);
            updatedEntity.setId(oldEntity.getId());
            return userRepository.save(updatedEntity) != null;
        }
        return false;
    }

    @Override
    public void deleteUser(String userName) {
        Optional<UserEntity> userEntity = userRepository.findByUserName(userName);
        userEntity.ifPresent(entity -> {
            assignRoles(entity.getUserName(), Collections.emptySet());
            userRepository.delete(entity);
        });
    }
}
