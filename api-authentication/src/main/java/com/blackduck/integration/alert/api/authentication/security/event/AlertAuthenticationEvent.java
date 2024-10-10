/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.authentication.security.event;

import com.blackduck.integration.alert.api.event.AlertEvent;
import com.blackduck.integration.alert.common.persistence.model.UserModel;

public class AlertAuthenticationEvent extends AlertEvent {
    private final UserModel user;

    public AlertAuthenticationEvent(UserModel user) {
        super(AuthenticationEventListener.DESTINATION_NAME);
        this.user = user;
    }

    public UserModel getUser() {
        return user;
    }

}
