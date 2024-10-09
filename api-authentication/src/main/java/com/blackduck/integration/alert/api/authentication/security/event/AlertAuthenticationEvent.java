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
