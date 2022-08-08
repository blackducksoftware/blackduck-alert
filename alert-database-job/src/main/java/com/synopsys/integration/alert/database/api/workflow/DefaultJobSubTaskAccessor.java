package com.synopsys.integration.alert.database.api.workflow;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.persistence.accessor.JobSubTaskAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.workflow.JobSubTaskStatusModel;
import com.synopsys.integration.alert.database.distribution.workflow.AuditCorrelationToNotificationRelation;
import com.synopsys.integration.alert.database.distribution.workflow.AuditCorrelationToNotificationRelationRepository;
import com.synopsys.integration.alert.database.distribution.workflow.JobSubTaskRepository;
import com.synopsys.integration.alert.database.distribution.workflow.JobSubTaskStatusEntity;

@Component
public class DefaultJobSubTaskAccessor implements JobSubTaskAccessor {
    private JobSubTaskRepository jobSubTaskRepository;
    private AuditCorrelationToNotificationRelationRepository auditCorrelationToNotificationRelationRepository;

    @Autowired
    public DefaultJobSubTaskAccessor(
        JobSubTaskRepository jobSubTaskRepository,
        AuditCorrelationToNotificationRelationRepository auditCorrelationToNotificationRelationRepository
    ) {
        this.jobSubTaskRepository = jobSubTaskRepository;
        this.auditCorrelationToNotificationRelationRepository = auditCorrelationToNotificationRelationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<JobSubTaskStatusModel> getSubTaskStatus(UUID parentEventId) {
        return jobSubTaskRepository.findById(parentEventId)
            .map(this::convertEntity);
    }

    @Override
    @Transactional
    public JobSubTaskStatusModel createSubTaskStatus(UUID parentEventId, UUID jobId, Long remainingTaskCount, List<Long> notificationIds) {
        UUID auditCorrelationId = UUID.randomUUID();
        JobSubTaskStatusEntity entity = new JobSubTaskStatusEntity(parentEventId, jobId, remainingTaskCount, auditCorrelationId);
        entity = jobSubTaskRepository.save(entity);

        for (Long notificationId : notificationIds) {
            auditCorrelationToNotificationRelationRepository.save(new AuditCorrelationToNotificationRelation(auditCorrelationId, notificationId));
        }
        return convertEntity(entity);
    }

    @Override
    @Transactional
    public Optional<JobSubTaskStatusModel> decrementTaskCount(UUID parentEventId) {
        Optional<JobSubTaskStatusEntity> entity = jobSubTaskRepository.findById(parentEventId);
        if (entity.isPresent()) {
            JobSubTaskStatusEntity item = entity.get();
            Long remainingTaskCount = item.getRemainingEvents() - 1;
            item = jobSubTaskRepository.save(new JobSubTaskStatusEntity(item.getParentEventId(), item.getJobId(), remainingTaskCount, item.getAuditCorrelationId()));
            entity = Optional.of(item);
        }
        return entity.map(this::convertEntity);
    }

    private JobSubTaskStatusModel convertEntity(JobSubTaskStatusEntity entity) {
        return new JobSubTaskStatusModel(entity.getParentEventId(), entity.getJobId(), entity.getRemainingEvents(), entity.getAuditCorrelationId());
    }
}
