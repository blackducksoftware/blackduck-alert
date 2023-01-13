package com.synopsys.integration.alert.database.api;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

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

    public DefaultJobExecutionStatusAccessor(JobExecutionRepository jobExecutionRepository, JobExecutionDurationsRepository jobExecutionDurationsRepository) {
        this.jobExecutionRepository = jobExecutionRepository;
        this.jobExecutionDurationsRepository = jobExecutionDurationsRepository;
    }

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

    public void saveExecutionStatus(JobExecutionStatusModel statusModel) {
        JobExecutionStatusDurationsEntity durations = convertDurationFromModel(statusModel.getJobConfigId(), statusModel.getDurations());
        JobExecutionStatusEntity jobExecutionStatus = convertFromModel(statusModel);
        jobExecutionRepository.save(jobExecutionStatus);
        jobExecutionDurationsRepository.save(durations);
    }

    private JobExecutionStatusModel convertToModel(JobExecutionStatusEntity entity) {
        JobExecutionStatusDurations durations = convertDurationToModel(entity.getJobExecutionDurations());
        return new JobExecutionStatusModel(
            entity.getJobConfigId(),
            entity.getNotificationCount(),
            entity.getSuccessCount(),
            entity.getFailureCount(),
            entity.getLatestStatus(),
            entity.getLastRun(),
            durations
        );
    }

    private JobExecutionStatusDurations convertDurationToModel(JobExecutionStatusDurationsEntity entity) {
        return new JobExecutionStatusDurations(
            entity.getJobDurationMillisec(),
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
            model.getNotificationCount(),
            model.getSuccessCount(),
            model.getFailureCount(),
            model.getLatestStatus(),
            model.getLastRun()
        );
    }

    private JobExecutionStatusDurationsEntity convertDurationFromModel(UUID jobConfigId, JobExecutionStatusDurations model) {
        return new JobExecutionStatusDurationsEntity(
            jobConfigId,
            model.getJobDurationMillisec(),
            model.getNotificationProcessingDuration().orElse(null),
            model.getChannelProcessingDuration().orElse(null),
            model.getIssueCreationDuration().orElse(null),
            model.getIssueCommentingDuration().orElse(null),
            model.getIssueTransitionDuration().orElse(null)
        );
    }
}
