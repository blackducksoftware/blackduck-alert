/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
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
