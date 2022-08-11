package com.synopsys.integration.alert.api.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.event.AlertEventHandler;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;

@Component
public class AuditFailedHandler implements AlertEventHandler<AuditFailedEvent> {
    private final ProcessingAuditAccessor processingAuditAccessor;

    @Autowired
    public AuditFailedHandler(ProcessingAuditAccessor processingAuditAccessor) {
        this.processingAuditAccessor = processingAuditAccessor;
    }

    @Override
    public void handle(AuditFailedEvent event) {
        processingAuditAccessor.setAuditEntryFailure(event.getJobId(), event.getNotificationIds(), event.getErrorMessage(), event.getStackTrace().orElse(null));
    }
}
