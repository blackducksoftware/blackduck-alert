package com.synopsys.integration.alert.database.api.workflow;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.persistence.accessor.JobSubTaskAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.workflow.JobSubTaskStatusModel;
import com.synopsys.integration.alert.database.distribution.workflow.JobSubTaskRepository;
import com.synopsys.integration.alert.database.distribution.workflow.JobSubTaskStatusEntity;
import com.synopsys.integration.alert.database.distribution.workflow.NotificationCorrelationToNotificationRelation;
import com.synopsys.integration.alert.database.distribution.workflow.NotificationCorrelationToNotificationRelationRepository;

@Component
public class DefaultJobSubTaskAccessor implements JobSubTaskAccessor {

    private JobSubTaskRepository jobSubTaskRepository;
    private NotificationCorrelationToNotificationRelationRepository notificationCorrelationToNotificationRelationRepository;

    @Autowired
    public DefaultJobSubTaskAccessor(
        JobSubTaskRepository jobSubTaskRepository,
        NotificationCorrelationToNotificationRelationRepository notificationCorrelationToNotificationRelationRepository
    ) {
        this.jobSubTaskRepository = jobSubTaskRepository;
        this.notificationCorrelationToNotificationRelationRepository = notificationCorrelationToNotificationRelationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<JobSubTaskStatusModel> getSubTaskStatus(UUID id) {
        return jobSubTaskRepository.findById(id)
            .map(this::convertEntity);
    }

    @Override
    @Transactional
    public JobSubTaskStatusModel createSubTaskStatus(UUID id, UUID jobId, UUID jobExecutionId, Long remainingTaskCount, Set<Long> notificationIds) {
        UUID notificationCorrelationId = UUID.randomUUID();
        JobSubTaskStatusEntity entity = new JobSubTaskStatusEntity(id, jobId, jobExecutionId, remainingTaskCount, notificationCorrelationId);
        entity = jobSubTaskRepository.save(entity);

        for (Long notificationId : notificationIds) {
            notificationCorrelationToNotificationRelationRepository.save(new NotificationCorrelationToNotificationRelation(notificationCorrelationId, notificationId));
        }
        return convertEntity(entity);
    }

    @Override
    @Transactional
    public Optional<JobSubTaskStatusModel> updateTaskCount(UUID id, Long remainingTaskCount) {
        Optional<JobSubTaskStatusEntity> entity = jobSubTaskRepository.findById(id);
        if (entity.isPresent()) {
            JobSubTaskStatusEntity currentEntity = entity.get();
            JobSubTaskStatusEntity updatedEntity = new JobSubTaskStatusEntity(
                currentEntity.getId(),
                currentEntity.getJobId(),
                currentEntity.getJobExecutionId(),
                remainingTaskCount,
                currentEntity.getNotificationCorrelationId()
            );
            entity = Optional.of(jobSubTaskRepository.save(updatedEntity));
        }
        return entity.map(this::convertEntity);
    }

    @Override
    @Transactional
    public Optional<JobSubTaskStatusModel> decrementTaskCount(UUID id) {
        Optional<JobSubTaskStatusEntity> entity = jobSubTaskRepository.findById(id);
        if (entity.isPresent()) {
            JobSubTaskStatusEntity item = entity.get();
            Long remainingTaskCount = item.getRemainingEvents() - 1;
            item = jobSubTaskRepository.save(new JobSubTaskStatusEntity(
                item.getId(),
                item.getJobId(),
                item.getJobExecutionId(),
                remainingTaskCount,
                item.getNotificationCorrelationId()
            ));
            entity = Optional.of(item);
        }
        return entity.map(this::convertEntity);
    }

    @Override
    @Transactional
    public Optional<JobSubTaskStatusModel> removeSubTaskStatus(UUID id) {
        Optional<JobSubTaskStatusEntity> entity = jobSubTaskRepository.findById(id);
        Optional<JobSubTaskStatusModel> model = Optional.empty();
        if (entity.isPresent()) {
            jobSubTaskRepository.deleteById(id);
            model = entity.map(this::convertEntity);
        }
        return model;
    }

    private JobSubTaskStatusModel convertEntity(JobSubTaskStatusEntity entity) {
        return new JobSubTaskStatusModel(entity.getId(), entity.getJobId(), entity.getJobExecutionId(), entity.getRemainingEvents(), entity.getNotificationCorrelationId());
    }
}
