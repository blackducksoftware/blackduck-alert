package com.synopsys.integration.alert.api.channel.issue.event.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.event.AlertMessageListener;

@Component
public class AuditFailedEventListener extends AlertMessageListener<AuditFailedEvent> {

    @Autowired
    public AuditFailedEventListener(
        Gson gson,
        TaskExecutor taskExecutor,
        AuditFailedHandler eventHandler
    ) {
        super(gson, taskExecutor, AuditFailedEvent.DEFAULT_DESTINATION_NAME, AuditFailedEvent.class, eventHandler);
    }
}
