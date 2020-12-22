/**
 * component
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
package com.synopsys.integration.alert.component.authentication.security.event;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.event.AlertEventListener;
import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.exception.AlertForbiddenOperationException;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.workflow.MessageReceiver;

@Component
public class AuthenticationEventListener extends MessageReceiver<AlertAuthenticationEvent> implements AlertEventListener {
    public static final String DESTINATION_NAME = "AuthenticationEventListener";

    private final UserAccessor userAccessor;

    public AuthenticationEventListener(Gson gson, UserAccessor userAccessor) {
        super(gson, AlertAuthenticationEvent.class);
        this.userAccessor = userAccessor;
    }

    @Override
    public String getDestinationName() {
        return DESTINATION_NAME;
    }

    @Override
    public void handleEvent(AlertAuthenticationEvent event) {
        UserModel user = event.getUser();
        if (null != user) {
            try {
                Optional<UserModel> userModel = userAccessor.getUser(user.getName());
                if (userModel.isPresent() && user.isExternal()) {
                    UserModel model = userModel.get();
                    UserModel updatedUser = UserModel.existingUser(
                        model.getId(),
                        user.getName(),
                        user.getPassword(),
                        user.getEmailAddress(),
                        user.getAuthenticationType(),
                        user.getRoles(),
                        user.isEnabled()
                    );
                    userAccessor.updateUser(updatedUser, true);
                } else {
                    userAccessor.addUser(user, true);
                }
            } catch (AlertForbiddenOperationException ignored) {
                // Cannot update an external user's credentials
            } catch (AlertConfigurationException ignored) {
                // User already exists. Nothing to do.
            }
        }
    }

}
