/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.security.event;

import com.synopsys.integration.alert.api.event.AlertEvent;
import com.synopsys.integration.alert.common.persistence.model.UserModel;

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
