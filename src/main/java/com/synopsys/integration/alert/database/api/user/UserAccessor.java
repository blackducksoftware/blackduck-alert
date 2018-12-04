/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.database.api.user;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.database.relation.UserRoleRelation;
import com.synopsys.integration.alert.database.relation.repository.UserRoleRepository;
import com.synopsys.integration.alert.database.user.RoleEntity;
import com.synopsys.integration.alert.database.user.RoleRepository;
import com.synopsys.integration.alert.database.user.UserEntity;
import com.synopsys.integration.alert.database.user.UserRepository;

@Component
public class UserAccessor {
    public static final String DEFAULT_ADMIN_USER = "sysadmin";
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder defaultPasswordEncoder;

    @Autowired
    public UserAccessor(final UserRepository userRepository, final RoleRepository roleRepository, final UserRoleRepository userRoleRepository, final PasswordEncoder defaultPasswordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.defaultPasswordEncoder = defaultPasswordEncoder;
    }

    @Transactional
    public List<UserModel> getUsers() {
        final List<UserEntity> userList = userRepository.findAll();
        return userList.stream().map(this::createModel).collect(Collectors.toList());
    }

    @Transactional
    public Optional<UserModel> getUser(final String username) {
        final Optional<UserModel> userResult;
        final Optional<UserEntity> entity = userRepository.findByUserName(username);
        if (entity.isPresent()) {
            userResult = Optional.ofNullable(createModel(entity.get()));
        } else {
            userResult = Optional.empty();
        }
        return userResult;
    }

    private UserModel createModel(final UserEntity user) {
        final List<UserRoleRelation> roleRelations = userRoleRepository.findAllByUserId(user.getId());
        final List<Long> roleIdsForUser = roleRelations.stream().map(UserRoleRelation::getRoleId).collect(Collectors.toList());
        final List<String> rolesForUser = roleRepository.getRoleNames(roleIdsForUser);
        return UserModel.of(user.getUserName(), user.getPassword(), rolesForUser);
    }

    @Transactional
    public UserModel addOrUpdateUser(final UserModel user) {
        final UserEntity userEntity = new UserEntity(user.getName(), defaultPasswordEncoder.encode(user.getPassword()));
        final List<String> roleNames = user.getRoles().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        final List<RoleEntity> roleEntities = roleRepository.findRoleEntitiesByRoleName(roleNames);
        final List<UserRoleRelation> roleRelations = new LinkedList<>();
        for (final RoleEntity role : roleEntities) {
            roleRelations.add(new UserRoleRelation(userEntity.getId(), role.getId()));
        }

        final Optional<UserEntity> existingUser = userRepository.findByUserName(user.getName());
        if (existingUser.isPresent()) {
            final Long userId = existingUser.get().getId();
            userEntity.setId(userId);
            userRoleRepository.deleteAllByUserId(userId);
        }
        userRoleRepository.saveAll(roleRelations);
        return createModel(userRepository.save(userEntity));
    }

    @Transactional
    public UserModel addUser(final String userName, final String password) {
        final List<String> emptyRoles = Collections.emptyList();
        return addOrUpdateUser(UserModel.of(userName, password, emptyRoles));
    }

    @Transactional
    public UserModel assignRoles(final String username, final Set<String> roles) {
        final Optional<UserEntity> entity = userRepository.findByUserName(username);
        if (entity.isPresent()) {
            return addOrUpdateUser(UserModel.of(entity.get().getUserName(), entity.get().getPassword(), roles));
        }
        return createModel(entity.get());
    }

    @Transactional
    public boolean changeUserPassword(final String username, final String newPassword) {
        final Optional<UserEntity> entity = userRepository.findByUserName(username);
        if (entity.isPresent()) {
            final UserEntity oldEntity = entity.get();
            final UserEntity updatedEntity = new UserEntity(oldEntity.getUserName(), defaultPasswordEncoder.encode(newPassword));
            updatedEntity.setId(oldEntity.getId());
            return userRepository.save(updatedEntity) != null;
        } else {
            return false;
        }
    }
}
