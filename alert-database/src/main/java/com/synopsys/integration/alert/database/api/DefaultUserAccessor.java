/**
 * alert-database
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.database.api;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.rest.model.UserModel;
import com.synopsys.integration.alert.database.user.RoleEntity;
import com.synopsys.integration.alert.database.user.RoleRepository;
import com.synopsys.integration.alert.database.user.UserEntity;
import com.synopsys.integration.alert.database.user.UserRepository;
import com.synopsys.integration.alert.database.user.UserRoleRelation;
import com.synopsys.integration.alert.database.user.UserRoleRepository;

@Component
@Transactional
public class DefaultUserAccessor {
    public static final String DEFAULT_ADMIN_USER = "sysadmin";
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder defaultPasswordEncoder;

    @Autowired
    public DefaultUserAccessor(final UserRepository userRepository, final RoleRepository roleRepository, final UserRoleRepository userRoleRepository, final PasswordEncoder defaultPasswordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.defaultPasswordEncoder = defaultPasswordEncoder;
    }

    public List<UserModel> getUsers() {
        final List<UserEntity> userList = userRepository.findAll();
        return userList.stream().map(this::createModel).collect(Collectors.toList());
    }

    public Optional<UserModel> getUser(final String username) {
        return userRepository.findByUserName(username).map(this::createModel);
    }

    private UserModel createModel(final UserEntity user) {
        final List<UserRoleRelation> roleRelations = userRoleRepository.findAllByUserId(user.getId());
        final List<Long> roleIdsForUser = roleRelations.stream().map(UserRoleRelation::getRoleId).collect(Collectors.toList());
        final Set<String> rolesForUser = new LinkedHashSet<>(roleRepository.getRoleNames(roleIdsForUser));
        return UserModel.of(user.getUserName(), user.getPassword(), user.getEmailAddress(), rolesForUser);
    }

    public UserModel addOrUpdateUser(final UserModel user) {
        return addOrUpdateUser(user, false);
    }

    public UserModel addOrUpdateUser(final UserModel user, final boolean passwordEncoded) {
        final String password = (passwordEncoded ? user.getPassword() : defaultPasswordEncoder.encode(user.getPassword()));
        final UserEntity userEntity = new UserEntity(user.getName(), password, user.getEmailAddress());
        final Collection<String> roles = user.getRoles();
        final List<RoleEntity> roleEntities = roleRepository.findRoleEntitiesByRoleName(roles);
        final List<UserRoleRelation> roleRelations = new LinkedList<>();

        final Optional<UserEntity> existingUser = userRepository.findByUserName(user.getName());
        if (existingUser.isPresent()) {
            final Long userId = existingUser.get().getId();
            userEntity.setId(userId);
            userRoleRepository.deleteAllByUserId(userId);
        }

        for (final RoleEntity role : roleEntities) {
            roleRelations.add(new UserRoleRelation(userEntity.getId(), role.getId()));
        }

        userRoleRepository.saveAll(roleRelations);
        return createModel(userRepository.save(userEntity));
    }

    public UserModel addUser(final String userName, final String password, final String emailAddress) {
        return addOrUpdateUser(UserModel.of(userName, password, emailAddress, Collections.emptySet()));
    }

    public boolean assignRoles(final String username, final Set<String> roles) {
        final Optional<UserEntity> entity = userRepository.findByUserName(username);
        boolean assigned = false;
        if (entity.isPresent()) {
            final UserModel model = addOrUpdateUser(UserModel.of(entity.get().getUserName(), entity.get().getPassword(), entity.get().getEmailAddress(), roles));
            assigned = model.getName().equals(username) && model.getRoles().size() == roles.size();
        }
        return assigned;
    }

    public boolean changeUserPassword(final String username, final String newPassword) {
        final Optional<UserEntity> entity = userRepository.findByUserName(username);
        if (entity.isPresent()) {
            final UserEntity oldEntity = entity.get();
            final UserEntity updatedEntity = new UserEntity(oldEntity.getUserName(), defaultPasswordEncoder.encode(newPassword), oldEntity.getEmailAddress());
            updatedEntity.setId(oldEntity.getId());
            return userRepository.save(updatedEntity) != null;
        }
        return false;
    }

    public boolean changeUserEmailAddress(final String username, final String emailAddress) {
        final Optional<UserEntity> entity = userRepository.findByUserName(username);
        if (entity.isPresent()) {
            final UserEntity oldEntity = entity.get();
            final UserEntity updatedEntity = new UserEntity(oldEntity.getUserName(), oldEntity.getPassword(), emailAddress);
            updatedEntity.setId(oldEntity.getId());
            return userRepository.save(updatedEntity) != null;
        }
        return false;
    }

    public void deleteUser(final String userName) {
        final Optional<UserEntity> userEntity = userRepository.findByUserName(userName);
        userEntity.ifPresent(entity -> {
            assignRoles(entity.getUserName(), Collections.emptySet());
            userRepository.delete(entity);
        });
    }
}
