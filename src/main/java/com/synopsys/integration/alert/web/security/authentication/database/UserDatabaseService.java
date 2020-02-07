/**
 * blackduck-alert
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
package com.synopsys.integration.alert.web.security.authentication.database;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.database.api.DefaultUserAccessor;

@Service
public class UserDatabaseService implements UserDetailsService {
    private final DefaultUserAccessor userAccessor;

    @Autowired
    public UserDatabaseService(final DefaultUserAccessor userAccessor) {
        this.userAccessor = userAccessor;
    }

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        return createUserDetails(username, userAccessor.getUser(username));
    }

    private UserDetails createUserDetails(final String userName, final Optional<UserModel> userModel) throws UsernameNotFoundException {
        final UserModel model = userModel.orElseThrow(() -> new UsernameNotFoundException(String.format("User %s not found", userName)));
        return new UserPrincipal(model);
    }
}
