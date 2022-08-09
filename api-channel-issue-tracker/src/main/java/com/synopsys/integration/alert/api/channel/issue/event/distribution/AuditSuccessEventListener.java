package com.synopsys.integration.alert.api.channel.issue.event.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.event.AlertMessageListener;

@Component
public class AuditSuccessEventListener extends AlertMessageListener<AuditSuccessEvent> {

    @Autowired
    public AuditSuccessEventListener(
        Gson gson,
        TaskExecutor taskExecutor,
        AuditSuccessHandler eventHandler
    ) {
        super(gson, taskExecutor, AuditSuccessEvent.DEFAULT_DESTINATION_NAME, AuditSuccessEvent.class, eventHandler);
    }
}
