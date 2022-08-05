package com.synopsys.integration.alert.database.api.workflow;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.persistence.accessor.JobSubTaskAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.workflow.JobSubTaskStatusModel;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.distribution.workflow.JobSubTaskRepository;

@Component
public class DefaultJobSubTaskAccessor implements JobSubTaskAccessor {
    private JobSubTaskRepository jobSubTaskRepository;
    private AuditNotificationRepository auditNotificationRepository;

    @Autowired
    public DefaultJobSubTaskAccessor(JobSubTaskRepository jobSubTaskRepository, AuditNotificationRepository auditNotificationRepository) {
        this.jobSubTaskRepository = jobSubTaskRepository;
        this.auditNotificationRepository = auditNotificationRepository;
    }

    @Override
    public JobSubTaskStatusModel getSubTaskStatus(UUID parentEventId) {
        return null;
    }

    @Override
    public JobSubTaskStatusModel createSubTaskStatus(UUID parentEventId, UUID jobId, Long remainingTaskCount, List<Long> notificationIds) {
        return null;
    }

    @Override
    public JobSubTaskStatusModel decrementTaskCount(UUID parentEventId) {
        return null;
    }

    @Override
    public void updateAuditEntryStatusFailed(UUID parentEventId) {
        // stubbed out method
    }
}
