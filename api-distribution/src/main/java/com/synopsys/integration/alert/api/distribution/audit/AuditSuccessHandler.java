package com.synopsys.integration.alert.api.distribution.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.event.AlertEventHandler;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;

@Component
public class AuditSuccessHandler implements AlertEventHandler<AuditSuccessEvent> {
    private final ProcessingAuditAccessor processingAuditAccessor;
    private final ExecutingJobManager executingJobManager;

    @Autowired
    public AuditSuccessHandler(ProcessingAuditAccessor processingAuditAccessor, ExecutingJobManager executingJobManager) {
        this.processingAuditAccessor = processingAuditAccessor;
        this.executingJobManager = executingJobManager;
    }

    @Override
    public void handle(AuditSuccessEvent event) {
        executingJobManager.endJobWithSuccess(event.getJobExecutionId());
    }
}
