package com.synopsys.integration.alert.api.distribution.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.event.AlertEventHandler;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingFailedAccessor;

@Component
public class AuditFailedHandler implements AlertEventHandler<AuditFailedEvent> {
    private final ProcessingAuditAccessor processingAuditAccessor;
    private final ProcessingFailedAccessor processingFailedAccessor;

    @Autowired
    public AuditFailedHandler(ProcessingAuditAccessor processingAuditAccessor, ProcessingFailedAccessor processingFailedAccessor) {
        this.processingAuditAccessor = processingAuditAccessor;
        this.processingFailedAccessor = processingFailedAccessor;
    }

    @Override
    public void handle(AuditFailedEvent event) {
        processingAuditAccessor.setAuditEntryFailure(
            event.getJobId(),
            event.getNotificationIds(),
            event.getCreatedTimestamp(),
            event.getErrorMessage(),
            event.getStackTrace().orElse(null)
        );
        if (event.getStackTrace().isPresent()) {
            processingFailedAccessor.setAuditFailure(
                event.getJobId(),
                event.getNotificationIds(),
                event.getCreatedTimestamp(),
                event.getErrorMessage(),
                event.getStackTrace().orElse("NO STACK TRACE")
            );
        } else {
            processingFailedAccessor.setAuditFailure(event.getJobId(), event.getNotificationIds(), event.getCreatedTimestamp(), event.getErrorMessage());
        }
    }
}
