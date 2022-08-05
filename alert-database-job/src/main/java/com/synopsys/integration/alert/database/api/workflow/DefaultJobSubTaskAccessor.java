package com.synopsys.integration.alert.database.api.workflow;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.persistence.accessor.JobSubTaskAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.workflow.JobSubTaskStatusModel;
import com.synopsys.integration.alert.database.distribution.workflow.AuditCorrelationToEntityRelation;
import com.synopsys.integration.alert.database.distribution.workflow.AuditCorrelationToEntityRelationRepository;
import com.synopsys.integration.alert.database.distribution.workflow.JobSubTaskRepository;
import com.synopsys.integration.alert.database.distribution.workflow.JobSubTaskStatusEntity;

@Component
public class DefaultJobSubTaskAccessor implements JobSubTaskAccessor {
    private JobSubTaskRepository jobSubTaskRepository;
    private AuditCorrelationToEntityRelationRepository auditCorrelationToEntityRelationRepository;

    private ProcessingAuditAccessor processingAuditAccessor;

    @Autowired
    public DefaultJobSubTaskAccessor(
        JobSubTaskRepository jobSubTaskRepository,
        AuditCorrelationToEntityRelationRepository auditCorrelationToEntityRelationRepository,
        ProcessingAuditAccessor processingAuditAccessor
    ) {
        this.jobSubTaskRepository = jobSubTaskRepository;
        this.auditCorrelationToEntityRelationRepository = auditCorrelationToEntityRelationRepository;
        this.processingAuditAccessor = processingAuditAccessor;
    }

    @Override
    public Optional<JobSubTaskStatusModel> getSubTaskStatus(UUID parentEventId) {
        return jobSubTaskRepository.findById(parentEventId)
            .map(this::convertEntity);
    }

    @Override
    public JobSubTaskStatusModel createSubTaskStatus(UUID parentEventId, UUID jobId, Long remainingTaskCount, List<Long> notificationIds) {
        UUID auditCorrelationId = UUID.randomUUID();
        JobSubTaskStatusEntity entity = new JobSubTaskStatusEntity(parentEventId, jobId, remainingTaskCount, auditCorrelationId);
        entity = jobSubTaskRepository.save(entity);

        for (Long notificationId : notificationIds) {
            //TODO fix the table and entity to use the notification ID
            auditCorrelationToEntityRelationRepository.save(new AuditCorrelationToEntityRelation(auditCorrelationId, notificationId));
        }
        return convertEntity(entity);
    }

    @Override
    public Optional<JobSubTaskStatusModel> decrementTaskCount(UUID parentEventId) {
        Optional<JobSubTaskStatusEntity> entity = jobSubTaskRepository.findById(parentEventId);
        if (entity.isPresent()) {
            JobSubTaskStatusEntity item = entity.get();
            Long remainingTaskCount = item.getRemainingEvents() - 1;
            item = jobSubTaskRepository.save(new JobSubTaskStatusEntity(item.getParentEventId(), item.getJobId(), remainingTaskCount, item.getAuditCorrelationId()));
            entity = Optional.ofNullable(item);
        }
        return entity.map(this::convertEntity);
    }

    @Override
    public void updateAuditEntryStatusFailed(UUID parentEventId, String message, Throwable exception) {
        Optional<JobSubTaskStatusEntity> entity = jobSubTaskRepository.findById(parentEventId);
        if (entity.isPresent()) {
            UUID jobId = entity.get().getJobId();
            UUID auditCorrelationId = entity.get().getAuditCorrelationId();
            PageRequest pageRequest = PageRequest.of(0, 200);
            Page<AuditCorrelationToEntityRelation> pageOfItems;
            pageOfItems = auditCorrelationToEntityRelationRepository.findAllByAuditCorrelationId(auditCorrelationId, pageRequest);
            int currentPage = pageOfItems.getNumber();
            while (currentPage <= pageOfItems.getTotalPages()) {
                Set<Long> notificationIds = pageOfItems.getContent().stream()
                    .map(AuditCorrelationToEntityRelation::getAuditEntryId)
                    .collect(Collectors.toSet());

                processingAuditAccessor.setAuditEntryFailure(jobId, notificationIds, message, exception);

                currentPage = pageOfItems.getNumber() + 1;
                pageRequest = PageRequest.of(currentPage, 200);
                pageOfItems = auditCorrelationToEntityRelationRepository.findAllByAuditCorrelationId(auditCorrelationId, pageRequest);
            }
        }
    }

    @Override
    public void updateAuditEntryStatusSucceeded(UUID parentEventId) {
        Optional<JobSubTaskStatusEntity> entity = jobSubTaskRepository.findById(parentEventId);
        if (entity.isPresent()) {
            UUID jobId = entity.get().getJobId();
            UUID auditCorrelationId = entity.get().getAuditCorrelationId();
            PageRequest pageRequest = PageRequest.of(0, 200);
            Page<AuditCorrelationToEntityRelation> pageOfItems;
            pageOfItems = auditCorrelationToEntityRelationRepository.findAllByAuditCorrelationId(auditCorrelationId, pageRequest);
            int currentPage = pageOfItems.getNumber();
            while (currentPage < pageOfItems.getTotalPages()) {
                Set<Long> notificationIds = pageOfItems.getContent().stream()
                    .map(AuditCorrelationToEntityRelation::getAuditEntryId)
                    .collect(Collectors.toSet());

                processingAuditAccessor.setAuditEntrySuccess(jobId, notificationIds);

                currentPage = pageOfItems.getNumber() + 1;
                pageRequest = PageRequest.of(currentPage, 200);
                pageOfItems = auditCorrelationToEntityRelationRepository.findAllByAuditCorrelationId(auditCorrelationId, pageRequest);
            }
        }
    }

    private JobSubTaskStatusModel convertEntity(JobSubTaskStatusEntity entity) {
        return new JobSubTaskStatusModel(entity.getParentEventId(), entity.getJobId(), entity.getRemainingEvents(), entity.getAuditCorrelationId());
    }
}
