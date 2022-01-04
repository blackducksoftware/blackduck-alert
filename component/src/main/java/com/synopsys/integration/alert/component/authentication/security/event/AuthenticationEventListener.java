/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.security.event;

import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.event.AlertMessageListener;

@Component
public class AuthenticationEventListener extends AlertMessageListener<AlertAuthenticationEvent> {
    public static final String DESTINATION_NAME = "AuthenticationEventListener";

    public AuthenticationEventListener(Gson gson, AuthenticationEventHandler eventHandler) {
        super(gson, DESTINATION_NAME, AlertAuthenticationEvent.class, eventHandler);
    }

}
