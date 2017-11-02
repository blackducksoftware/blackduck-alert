/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.datasource.entity.model;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.hub.alert.datasource.entity.UserConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.UserConfigRepository;

public class UserConfigDataService {
    private final UserConfigRepository userConfigRepository;

    public UserConfigDataService(final UserConfigRepository userConfigRepository) {
        this.userConfigRepository = userConfigRepository;
    }

    public List<UserConfigWrapper> getAllUsers() {
        final List<UserConfigEntity> usersInDatabase = userConfigRepository.findAll();
        final List<UserConfigWrapper> wrappedUsers = new ArrayList<>(usersInDatabase.size());
        if (!usersInDatabase.isEmpty()) {
            usersInDatabase.forEach(user -> {
                wrappedUsers.add(new UserConfigWrapper(user.getId(), user.getUsername()));
            });
        }
        return wrappedUsers;
    }

    public UserConfigWrapper getOneUser(final Long id) {
        final UserConfigEntity entity = userConfigRepository.getOne(id);
        return new UserConfigWrapper(entity.getId(), entity.getUsername());
    }

}
