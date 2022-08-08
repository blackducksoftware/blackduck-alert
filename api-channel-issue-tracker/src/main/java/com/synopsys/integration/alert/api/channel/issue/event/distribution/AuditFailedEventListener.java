package com.synopsys.integration.alert.api.channel.issue.event.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.event.AlertEventHandler;
import com.synopsys.integration.alert.api.event.AlertMessageListener;

@Component
public class AuditFailedEventListener extends AlertMessageListener<AuditFailedEvent> {
    public static final String DEFAULT_DESTINATION_NAME = "audit_failed_event";

    @Autowired
    public AuditFailedEventListener(
        Gson gson,
        TaskExecutor taskExecutor,
        Class<AuditFailedEvent> eventClass,
        AlertEventHandler<AuditFailedEvent> eventHandler
    ) {
        super(gson, taskExecutor, DEFAULT_DESTINATION_NAME, eventClass, eventHandler);
    }
}
