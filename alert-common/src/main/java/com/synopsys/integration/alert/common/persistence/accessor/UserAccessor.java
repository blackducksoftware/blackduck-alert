/**
 * alert-common
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
package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.synopsys.integration.alert.common.persistence.model.UserModel;

public interface UserAccessor {

    List<UserModel> getUsers();

    Optional<UserModel> getUser(final String username);

    UserModel addOrUpdateUser(final UserModel user);

    UserModel addOrUpdateUser(final UserModel user, final boolean passwordEncoded);

    UserModel addUser(final String userName, final String password, final String emailAddress);

    boolean assignRoles(final String username, final Set<Long> roleIds);

    boolean changeUserPassword(final String username, final String newPassword);

    boolean changeUserEmailAddress(final String username, final String emailAddress);

    void deleteUser(final String userName);

}
