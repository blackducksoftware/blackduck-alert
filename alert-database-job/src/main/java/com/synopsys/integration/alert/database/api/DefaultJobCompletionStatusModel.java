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
import com.synopsys.integration.alert.common.persistence.model.job.executions.JobCompletionStatusDurations;
import com.synopsys.integration.alert.common.persistence.model.job.executions.JobCompletionStatusModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedQueryDetails;
import com.synopsys.integration.alert.database.job.execution.JobCompletionDurationsRepository;
import com.synopsys.integration.alert.database.job.execution.JobCompletionRepository;
import com.synopsys.integration.alert.database.job.execution.JobCompletionStatusDurationsEntity;
import com.synopsys.integration.alert.database.job.execution.JobCompletionStatusEntity;

@Component
public class DefaultJobCompletionStatusModel implements com.synopsys.integration.alert.common.persistence.accessor.JobCompletionStatusModel {

    private final JobCompletionRepository jobCompletionRepository;
    private final JobCompletionDurationsRepository jobCompletionDurationsRepository;

    @Autowired
    public DefaultJobCompletionStatusModel(JobCompletionRepository jobCompletionRepository, JobCompletionDurationsRepository jobCompletionDurationsRepository) {
        this.jobCompletionRepository = jobCompletionRepository;
        this.jobCompletionDurationsRepository = jobCompletionDurationsRepository;
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
    public Optional<JobCompletionStatusModel> getJobExecutionStatus(UUID jobConfigId) {
        return jobCompletionRepository.findById(jobConfigId).map(this::convertToModel);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
    public AlertPagedModel<JobCompletionStatusModel> getJobExecutionStatus(AlertPagedQueryDetails pagedQueryDetails) {
        Sort sort = (pagedQueryDetails.getSortName().isPresent() && pagedQueryDetails.getSortOrder().isPresent()) ?
            Sort.by(pagedQueryDetails.getSortOrder().get(), pagedQueryDetails.getSortName().get()) :
            Sort.unsorted();
        PageRequest pageRequest = PageRequest.of(pagedQueryDetails.getOffset(), pagedQueryDetails.getLimit(), sort);

        Page<JobCompletionStatusEntity> entities;
        if (pagedQueryDetails.getSearchTerm().filter(StringUtils::isNotBlank).isPresent()) {
            entities = jobCompletionRepository.findBySearchTerm(pagedQueryDetails.getSearchTerm().get(), pageRequest);
        } else {
            entities = jobCompletionRepository.findAll(pageRequest);
        }
        List<JobCompletionStatusModel> pageContents = entities.map(this::convertToModel).getContent();
        return new AlertPagedModel<>(entities.getTotalPages(), pagedQueryDetails.getOffset(), pagedQueryDetails.getLimit(), pageContents);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveExecutionStatus(JobCompletionStatusModel statusModel) {
        JobCompletionStatusModel updatedStatusModel = updateCompletedJobStatus(statusModel);
        JobCompletionStatusDurationsEntity durations = convertDurationFromModel(updatedStatusModel.getJobConfigId(), updatedStatusModel.getDurations());
        JobCompletionStatusEntity jobExecutionStatus = convertFromModel(updatedStatusModel);
        jobCompletionRepository.save(jobExecutionStatus);
        jobCompletionDurationsRepository.save(durations);
    }

    private JobCompletionStatusModel convertToModel(JobCompletionStatusEntity entity) {
        JobCompletionStatusDurations durations = convertDurationToModel(jobCompletionDurationsRepository.findById(entity.getJobConfigId())
            .orElseGet(() -> createEmptyDurations(entity.getJobConfigId())));
        return new JobCompletionStatusModel(
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

    private JobCompletionStatusDurations convertDurationToModel(JobCompletionStatusDurationsEntity entity) {
        return new JobCompletionStatusDurations(
            entity.getJobDuration(),
            entity.getNotificationProcessingDuration(),
            entity.getChannelProcessingDuration(),
            entity.getIssueCreationDuration(),
            entity.getIssueCommentingDuration(),
            entity.getIssueTransitionDuration()
        );
    }

    private JobCompletionStatusEntity convertFromModel(JobCompletionStatusModel model) {
        return new JobCompletionStatusEntity(
            model.getJobConfigId(),
            model.getLatestNotificationCount(),
            model.getTotalNotificationCount(),
            model.getSuccessCount(),
            model.getFailureCount(),
            model.getLatestStatus(),
            model.getLastRun()
        );
    }

    private JobCompletionStatusDurationsEntity convertDurationFromModel(UUID jobConfigId, JobCompletionStatusDurations model) {
        return new JobCompletionStatusDurationsEntity(
            jobConfigId,
            model.getJobDuration(),
            model.getNotificationProcessingDuration(),
            model.getChannelProcessingDuration(),
            model.getIssueCreationDuration(),
            model.getIssueCommentingDuration(),
            model.getIssueTransitionDuration()
        );
    }

    private JobCompletionStatusDurationsEntity createEmptyDurations(UUID jobConfigId) {
        return new JobCompletionStatusDurationsEntity(
            jobConfigId,
            0L,
            0L,
            0L,
            0L,
            0L,
            0L
        );
    }

    private JobCompletionStatusModel updateCompletedJobStatus(JobCompletionStatusModel latestData) {
        return getJobExecutionStatus(latestData.getJobConfigId())
            .map(savedStatus -> updateCompletedJobStatus(savedStatus, latestData))
            .orElse(latestData);
    }

    private JobCompletionStatusModel updateCompletedJobStatus(JobCompletionStatusModel savedStatus, JobCompletionStatusModel latestData) {
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

        return new JobCompletionStatusModel(
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

    private JobCompletionStatusDurations calculateDurations(JobCompletionStatusDurations latestData, JobCompletionStatusDurations savedData) {
        return new JobCompletionStatusDurations(
            calculateAverage(latestData.getJobDuration(), savedData.getJobDuration()),
            calculateAverage(latestData.getNotificationProcessingDuration(), savedData.getNotificationProcessingDuration()),
            calculateAverage(latestData.getChannelProcessingDuration(), savedData.getChannelProcessingDuration()),
            calculateAverage(latestData.getIssueCreationDuration(), savedData.getIssueCreationDuration()),
            calculateAverage(latestData.getIssueCommentingDuration(), savedData.getIssueCommentingDuration()),
            calculateAverage(latestData.getIssueTransitionDuration(), savedData.getIssueTransitionDuration())
        );
    }

    private long calculateAverage(long firstValue, long secondValue) {
        if (firstValue == 0 && secondValue == 0) {
            return 0L;
        }
        return (firstValue + secondValue) / 2;
    }
}
