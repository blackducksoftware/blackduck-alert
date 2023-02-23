package com.synopsys.integration.alert.api.authentication.security.event;

import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.event.AlertMessageListener;

@Component
public class AuthenticationEventListener extends AlertMessageListener<AlertAuthenticationEvent> {
    public static final String DESTINATION_NAME = "AuthenticationEventListener";

    public AuthenticationEventListener(Gson gson, TaskExecutor taskExecutor, AuthenticationEventHandler eventHandler) {
        super(gson, taskExecutor, DESTINATION_NAME, AlertAuthenticationEvent.class, eventHandler);
    }

}
