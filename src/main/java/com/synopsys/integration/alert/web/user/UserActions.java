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
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.accessor.AuthorizationUtility;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.web.model.UserRoleModel;

@Component
@Transactional
public class UserActions {
    private AuthorizationUtility authorizationUtility;
    private UserAccessor userAccessor;

    public UserActions(AuthorizationUtility authorizationUtility, UserAccessor userAccessor) {
        this.authorizationUtility = authorizationUtility;
        this.userAccessor = userAccessor;
    }

    public Collection<UserRoleModel> getUsers() {
        return userAccessor.getUsers().stream()
                   .map(this::convertToCustomUserRoleModel)
                   .collect(Collectors.toList());
    }

    public Optional<UserRoleModel> getUser(String username) {
        return userAccessor.getUser(username).map(this::convertToCustomUserRoleModel);
    }

    public UserRoleModel createUser(String userName, String password, String emailAddress) throws AlertDatabaseConstraintException {
        UserModel userModel = userAccessor.addUser(userName, password, emailAddress);
        return convertToCustomUserRoleModel(userModel);
    }

    public void deleteUser(String userName) throws AlertDatabaseConstraintException {
        userAccessor.deleteUser(userName);
    }

    public boolean updatePassword(String userName, String newPassword) {
        return userAccessor.changeUserPassword(userName, newPassword);
    }

    public boolean updateEmail(String userName, String emailAddress) {
        return userAccessor.changeUserEmailAddress(userName, emailAddress);
    }

    private UserRoleModel convertToCustomUserRoleModel(UserModel userModel) {
        return new UserRoleModel(
            userModel.getName(),
            userModel.getPassword(),
            userModel.getEmailAddress(),
            userModel.getRoleNames(),
            userModel.isExpired(),
            userModel.isLocked(),
            userModel.isPasswordExpired(),
            userModel.isEnabled());
    }
}
