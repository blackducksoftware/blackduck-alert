package com.synopsys.integration.alert.api.distribution.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.event.AlertEventHandler;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;

@Component
public class AuditSuccessHandler implements AlertEventHandler<AuditSuccessEvent> {
    private final ProcessingAuditAccessor processingAuditAccessor;

    @Autowired
    public AuditSuccessHandler(ProcessingAuditAccessor processingAuditAccessor) {
        this.processingAuditAccessor = processingAuditAccessor;
    }

    @Override
    public void handle(AuditSuccessEvent event) {
        processingAuditAccessor.setAuditEntrySuccess(event.getJobId(), event.getNotificationIds(), event.getCreatedTimestamp());
    }
}
