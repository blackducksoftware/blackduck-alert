package com.synopsys.integration.alert.database.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.persistence.accessor.JobExecutionStatusAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.executions.JobExecutionStatusDurations;
import com.synopsys.integration.alert.common.persistence.model.job.executions.JobExecutionStatusModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedQueryDetails;
import com.synopsys.integration.alert.database.job.execution.JobExecutionDurationsRepository;
import com.synopsys.integration.alert.database.job.execution.JobExecutionRepository;
import com.synopsys.integration.alert.database.job.execution.JobExecutionStatusDurationsEntity;
import com.synopsys.integration.alert.database.job.execution.JobExecutionStatusEntity;

@Component
public class DefaultJobExecutionStatusAccessor implements JobExecutionStatusAccessor {

    private final JobExecutionRepository jobExecutionRepository;
    private final JobExecutionDurationsRepository jobExecutionDurationsRepository;

    @Autowired
    public DefaultJobExecutionStatusAccessor(JobExecutionRepository jobExecutionRepository, JobExecutionDurationsRepository jobExecutionDurationsRepository) {
        this.jobExecutionRepository = jobExecutionRepository;
        this.jobExecutionDurationsRepository = jobExecutionDurationsRepository;
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
    public Optional<JobExecutionStatusModel> getJobExecutionStatus(UUID jobConfigId) {
        return jobExecutionRepository.findById(jobConfigId).map(this::convertToModel);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
    public AlertPagedModel<JobExecutionStatusModel> getJobExecutionStatus(AlertPagedQueryDetails pagedQueryDetails) {
        Sort sort = (pagedQueryDetails.getSortName().isPresent() && pagedQueryDetails.getSortOrder().isPresent()) ?
            Sort.by(pagedQueryDetails.getSortOrder().get(), pagedQueryDetails.getSortName().get()) :
            Sort.unsorted();
        PageRequest pageRequest = PageRequest.of(pagedQueryDetails.getOffset(), pagedQueryDetails.getLimit(), sort);

        Page<JobExecutionStatusEntity> entities;
        if (pagedQueryDetails.getSearchTerm().filter(StringUtils::isNotBlank).isPresent()) {
            entities = jobExecutionRepository.findBySearchTerm(pagedQueryDetails.getSearchTerm().get(), pageRequest);
        } else {
            entities = jobExecutionRepository.findAll(pageRequest);
        }
        List<JobExecutionStatusModel> pageContents = entities.map(this::convertToModel).getContent();
        return new AlertPagedModel<>(entities.getTotalPages(), pagedQueryDetails.getOffset(), pagedQueryDetails.getLimit(), pageContents);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveExecutionStatus(JobExecutionStatusModel statusModel) {
        JobExecutionStatusModel updatedStatusModel = updateCompletedJobStatus(statusModel);
        JobExecutionStatusDurationsEntity durations = convertDurationFromModel(updatedStatusModel.getJobConfigId(), updatedStatusModel.getDurations());
        JobExecutionStatusEntity jobExecutionStatus = convertFromModel(updatedStatusModel);
        jobExecutionRepository.save(jobExecutionStatus);
        jobExecutionDurationsRepository.save(durations);
    }

    private JobExecutionStatusModel convertToModel(JobExecutionStatusEntity entity) {
        JobExecutionStatusDurations durations = convertDurationToModel(jobExecutionDurationsRepository.findById(entity.getJobConfigId())
            .orElseGet(() -> createEmptyDurations(entity.getJobConfigId())));
        return new JobExecutionStatusModel(
            entity.getJobConfigId(),
            entity.getLatestNotificationCount(),
            entity.getTotalNotificationCount(),
            entity.getSuccessCount(),
            entity.getFailureCount(),
            entity.getLatestStatus(),
            entity.getLastRun(),
            durations
        );
    }

    private JobExecutionStatusDurations convertDurationToModel(JobExecutionStatusDurationsEntity entity) {
        return new JobExecutionStatusDurations(
            entity.getJobDuration(),
            entity.getNotificationProcessingDuration(),
            entity.getChannelProcessingDuration(),
            entity.getIssueCreationDuration(),
            entity.getIssueCommentingDuration(),
            entity.getIssueTransitionDuration()
        );
    }

    private JobExecutionStatusEntity convertFromModel(JobExecutionStatusModel model) {
        return new JobExecutionStatusEntity(
            model.getJobConfigId(),
            model.getLatestNotificationCount(),
            model.getTotalNotificationCount(),
            model.getSuccessCount(),
            model.getFailureCount(),
            model.getLatestStatus(),
            model.getLastRun()
        );
    }

    private JobExecutionStatusDurationsEntity convertDurationFromModel(UUID jobConfigId, JobExecutionStatusDurations model) {
        return new JobExecutionStatusDurationsEntity(
            jobConfigId,
            model.getJobDuration(),
            model.getNotificationProcessingDuration().orElse(null),
            model.getChannelProcessingDuration().orElse(null),
            model.getIssueCreationDuration().orElse(null),
            model.getIssueCommentingDuration().orElse(null),
            model.getIssueTransitionDuration().orElse(null)
        );
    }

    private JobExecutionStatusDurationsEntity createEmptyDurations(UUID jobConfigId) {
        return new JobExecutionStatusDurationsEntity(
            jobConfigId,
            0L,
            null,
            null,
            null,
            null,
            null
        );
    }

    private JobExecutionStatusModel updateCompletedJobStatus(JobExecutionStatusModel latestData) {
        return getJobExecutionStatus(latestData.getJobConfigId())
            .map(savedStatus -> updateCompletedJobStatus(savedStatus, latestData))
            .orElse(latestData);
    }

    private JobExecutionStatusModel updateCompletedJobStatus(JobExecutionStatusModel savedStatus, JobExecutionStatusModel latestData) {
        long successCount = savedStatus.getSuccessCount();
        long failureCount = savedStatus.getFailureCount();
        AuditEntryStatus jobStatus = AuditEntryStatus.valueOf(latestData.getLatestStatus());

        if (jobStatus == AuditEntryStatus.SUCCESS) {
            successCount = savedStatus.getSuccessCount() + latestData.getSuccessCount();
        }

        if (jobStatus == AuditEntryStatus.FAILURE) {
            failureCount = savedStatus.getFailureCount() + latestData.getFailureCount();
        }

        long totalNotificationCount = savedStatus.getTotalNotificationCount() + latestData.getTotalNotificationCount();

        return new JobExecutionStatusModel(
            latestData.getJobConfigId(),
            latestData.getLatestNotificationCount(),
            totalNotificationCount,
            successCount,
            failureCount,
            jobStatus.name(),
            latestData.getLastRun(),
            calculateDurations(latestData.getDurations(), savedStatus.getDurations())
        );
    }

    private JobExecutionStatusDurations calculateDurations(JobExecutionStatusDurations latestData, JobExecutionStatusDurations savedData) {
        return new JobExecutionStatusDurations(
            calculateAverage(latestData.getJobDuration(), savedData.getJobDuration()),
            calculateAverage(latestData.getNotificationProcessingDuration().orElse(0L), savedData.getNotificationProcessingDuration().orElse(0L)),
            calculateAverage(latestData.getChannelProcessingDuration().orElse(0L), savedData.getChannelProcessingDuration().orElse(0L)),
            calculateAverage(latestData.getIssueCreationDuration().orElse(0L), savedData.getIssueCreationDuration().orElse(0L)),
            calculateAverage(latestData.getIssueCommentingDuration().orElse(0L), savedData.getIssueCommentingDuration().orElse(0L)),
            calculateAverage(latestData.getIssueTransitionDuration().orElse(0L), savedData.getIssueTransitionDuration().orElse(0L))
        );
    }

    private Long calculateAverage(Long firstValue, Long secondValue) {
        if (firstValue == 0 && secondValue == 0) {
            return 0L;
        }
        return (firstValue + secondValue) / 2;
    }
}
