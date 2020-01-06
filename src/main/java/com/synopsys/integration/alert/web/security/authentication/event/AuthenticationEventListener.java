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
package com.synopsys.integration.alert.web.security.authentication.event;

import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.event.AlertEventListener;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.workflow.MessageReceiver;

@Component
public class AuthenticationEventListener extends MessageReceiver<AlertAuthenticationEvent> implements AlertEventListener {
    public static final String DESTINATION_NAME = "AuthenticationEventListener";

    private UserAccessor userAccessor;

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
                userAccessor.addUser(user, true);
            } catch (AlertDatabaseConstraintException ignored) {
                // User already exists. Nothing to do.
            }
        }
    }

}
