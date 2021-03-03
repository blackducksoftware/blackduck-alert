/*
 * component
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.security.event;

import java.util.Optional;

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
                Optional<UserModel> userModel = userAccessor.getUser(user.getName());
                if (userModel.isPresent() && user.isExternal()) {
                    UserModel model = userModel.get();
                    UserModel updatedUser = UserModel.existingUser(
                        model.getId(), user.getName(), user.getPassword(), user.getEmailAddress(),
                        user.getAuthenticationType(), user.getRoles(), user.isEnabled());
                    userAccessor.updateUser(updatedUser, true);
                } else {
                    userAccessor.addUser(user, true);
                }
            } catch (AlertDatabaseConstraintException ignored) {
                // User already exists. Nothing to do.
            }
        }
    }

}
