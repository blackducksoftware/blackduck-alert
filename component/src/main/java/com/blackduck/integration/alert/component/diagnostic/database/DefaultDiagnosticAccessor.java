/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.diagnostic.database;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.blackduck.integration.alert.api.distribution.execution.AggregatedExecutionResults;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJob;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobStage;
import com.blackduck.integration.alert.api.distribution.execution.JobStage;
import com.blackduck.integration.alert.common.enumeration.AuditEntryStatus;
import com.blackduck.integration.alert.common.persistence.accessor.DiagnosticAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.JobCompletionStatusModelAccessor;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobModelData;
import com.blackduck.integration.alert.common.persistence.model.job.executions.JobCompletionStatusDurations;
import com.blackduck.integration.alert.common.persistence.model.job.executions.JobCompletionStatusModel;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;
import com.blackduck.integration.alert.common.rest.model.AlertPagedQueryDetails;
import com.blackduck.integration.alert.common.util.DateUtils;
import com.blackduck.integration.alert.component.diagnostic.model.AuditDiagnosticModel;
import com.blackduck.integration.alert.component.diagnostic.model.CompletedJobDiagnosticModel;
import com.blackduck.integration.alert.component.diagnostic.model.CompletedJobDurationDiagnosticModel;
import com.blackduck.integration.alert.component.diagnostic.model.CompletedJobStageDurationModel;
import com.blackduck.integration.alert.component.diagnostic.model.CompletedJobsDiagnosticModel;
import com.blackduck.integration.alert.component.diagnostic.model.DiagnosticModel;
import com.blackduck.integration.alert.component.diagnostic.model.JobExecutionDiagnosticModel;
import com.blackduck.integration.alert.component.diagnostic.model.JobExecutionsDiagnosticModel;
import com.blackduck.integration.alert.component.diagnostic.model.JobStageDiagnosticModel;
import com.blackduck.integration.alert.component.diagnostic.model.NotificationDiagnosticModel;
import com.blackduck.integration.alert.component.diagnostic.model.NotificationTypeCount;
import com.blackduck.integration.alert.component.diagnostic.model.ProviderNotificationCounts;
import com.blackduck.integration.alert.component.diagnostic.model.RabbitMQDiagnosticModel;
import com.blackduck.integration.alert.component.diagnostic.model.SystemDiagnosticModel;
import com.blackduck.integration.alert.component.diagnostic.utility.RabbitMQDiagnosticUtility;
import com.blackduck.integration.alert.database.audit.AuditEntryRepository;
import com.blackduck.integration.alert.database.job.api.StaticJobAccessor;
import com.blackduck.integration.alert.database.notification.NotificationContentRepository;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;

@Component
public class DefaultDiagnosticAccessor implements DiagnosticAccessor {
    private final NotificationContentRepository notificationContentRepository;
    private final AuditEntryRepository auditEntryRepository;
    private final RabbitMQDiagnosticUtility rabbitMQDiagnosticUtility;

    private final StaticJobAccessor jobAccessor;
    private final ExecutingJobManager executingJobManager;
    private final JobCompletionStatusModelAccessor completedJobStatusAccessor;

    @Autowired
    public DefaultDiagnosticAccessor(
        NotificationContentRepository notificationContentRepository,
        AuditEntryRepository auditEntryRepository,
        RabbitMQDiagnosticUtility rabbitMQDiagnosticUtility,
        StaticJobAccessor staticJobAccessor,
        JobCompletionStatusModelAccessor completedJobStatusAccessor,
        ExecutingJobManager executingJobManager
    ) {
        this.notificationContentRepository = notificationContentRepository;
        this.auditEntryRepository = auditEntryRepository;
        this.rabbitMQDiagnosticUtility = rabbitMQDiagnosticUtility;
        this.jobAccessor = staticJobAccessor;
        this.completedJobStatusAccessor = completedJobStatusAccessor;
        this.executingJobManager = executingJobManager;
    }

    @Override
    @Transactional(readOnly = true)
    public DiagnosticModel getDiagnosticInfo() {
        NotificationDiagnosticModel notificationDiagnosticModel = getNotificationDiagnosticInfo();
        AuditDiagnosticModel auditDiagnosticModel = getAuditDiagnosticInfo();
        SystemDiagnosticModel systemDiagnosticModel = getSystemInfo();
        RabbitMQDiagnosticModel rabbitMQDiagnosticModel = rabbitMQDiagnosticUtility.getRabbitMQDiagnostics();
        JobExecutionsDiagnosticModel jobExecutionsDiagnosticModel = getExecutingJobModel();
        CompletedJobsDiagnosticModel completedJobsDiagnosticModel = getCompletedJobModel();
        return new DiagnosticModel(
            LocalDateTime.now().toString(),
            notificationDiagnosticModel,
            auditDiagnosticModel,
            systemDiagnosticModel,
            rabbitMQDiagnosticModel,
            completedJobsDiagnosticModel,
            jobExecutionsDiagnosticModel
        );
    }

    private NotificationDiagnosticModel getNotificationDiagnosticInfo() {
        long numberOfNotifications = notificationContentRepository.count();
        long numberOfNotificationsProcessed = notificationContentRepository.countByProcessed(true);
        long numberOfNotificationsUnprocessed = notificationContentRepository.countByProcessed(false);
        List<ProviderNotificationCounts> providerNotificationCounts = getProviderNotificationCounts();
        return new NotificationDiagnosticModel(numberOfNotifications, numberOfNotificationsProcessed, numberOfNotificationsUnprocessed, providerNotificationCounts);
    }

    private List<ProviderNotificationCounts> getProviderNotificationCounts() {
        Set<Long> providerConfigIds = new LinkedHashSet<>();
        int pageSize = 100;
        int pageNumber = 0;
        AlertPagedModel<DistributionJobModel> page = jobAccessor.getPageOfJobs(pageNumber, pageSize);
        while (page.getCurrentPage() < page.getTotalPages()) {
            providerConfigIds.addAll(page.getModels()
                .stream()
                .map(DistributionJobModel::getBlackDuckGlobalConfigId)
                .collect(Collectors.toSet()));
            pageNumber++;
            page = jobAccessor.getPageOfJobs(pageNumber, pageSize);
        }
        List<ProviderNotificationCounts> providerCounts = new LinkedList<>();
        for (Long providerConfigId : providerConfigIds) {
            List<NotificationTypeCount> notificationTypeCounts = new LinkedList<>();
            for (NotificationType notificationType : NotificationType.values()) {
                long count = notificationContentRepository.countByProviderConfigIdAndNotificationType(providerConfigId, notificationType.name());
                notificationTypeCounts.add(new NotificationTypeCount(notificationType, count));
            }
            providerCounts.add(new ProviderNotificationCounts(providerConfigId, notificationTypeCounts));
        }
        return providerCounts;
    }

    private AuditDiagnosticModel getAuditDiagnosticInfo() {
        long numberOfAuditEntriesSuccessful = auditEntryRepository.countByStatus(AuditEntryStatus.SUCCESS.name());
        long numberOfAuditEntriesFailed = auditEntryRepository.countByStatus(AuditEntryStatus.FAILURE.name());
        long numberOfAuditEntriesPending = auditEntryRepository.countByStatus(AuditEntryStatus.PENDING.name());
        return new AuditDiagnosticModel(
            numberOfAuditEntriesSuccessful,
            numberOfAuditEntriesFailed,
            numberOfAuditEntriesPending,
            auditEntryRepository.getAverageAuditEntryCompletionTime().orElse(AuditDiagnosticModel.NO_AUDIT_CONTENT_MESSAGE)
        );
    }

    private SystemDiagnosticModel getSystemInfo() {
        Runtime runtime = Runtime.getRuntime();
        return new SystemDiagnosticModel(runtime.availableProcessors(), runtime.maxMemory(), runtime.totalMemory(), runtime.freeMemory());
    }

    private JobExecutionsDiagnosticModel getExecutingJobModel() {
        AggregatedExecutionResults executionResults = executingJobManager.aggregateExecutingJobData();
        List<JobExecutionDiagnosticModel> jobExecutions = getExecutionData();
        return new JobExecutionsDiagnosticModel(
            executionResults.getTotalJobsInSystem(),
            executionResults.getPendingJobs(),
            executionResults.getSuccessFulJobs(),
            executionResults.getFailedJobs(),
            jobExecutions
        );
    }

    private CompletedJobsDiagnosticModel getCompletedJobModel() {
        List<CompletedJobDiagnosticModel> jobStatusData = new LinkedList<>();
        int pageNumber = 0;
        int pageSize = 100;
        AlertPagedModel<JobCompletionStatusModel> page = completedJobStatusAccessor.getJobExecutionStatus(new AlertPagedQueryDetails(pageNumber, pageSize));
        while (pageNumber < page.getTotalPages()) {
            jobStatusData.addAll(page.getModels().stream()
                .map(this::convertJobStatusData)
                .collect(Collectors.toList()));
            pageNumber++;
            page = completedJobStatusAccessor.getJobExecutionStatus(new AlertPagedQueryDetails(pageNumber, pageSize));
        }

        return new CompletedJobsDiagnosticModel(jobStatusData);
    }

    private List<JobExecutionDiagnosticModel> getExecutionData() {
        List<JobExecutionDiagnosticModel> jobExecutions = new LinkedList<>();
        int pageSize = 100;
        int pageNumber = 1;
        AlertPagedModel<ExecutingJob> page = executingJobManager.getExecutingJobs(pageNumber, pageSize);
        while (page.getCurrentPage() <= page.getTotalPages()) {
            jobExecutions.addAll(page.getModels().stream()
                .map(this::convertExecutionData)
                .toList());
            pageNumber++;
            page = executingJobManager.getExecutingJobs(pageNumber, pageSize);
        }

        return jobExecutions;
    }

    private JobExecutionDiagnosticModel convertExecutionData(ExecutingJob job) {
        List<JobStageDiagnosticModel> stageData = job.getStages().values()
            .stream()
            .map(this::convertJobStageData)
            .toList();
        Optional<DistributionJobModel> distributionJobModel = jobAccessor.getJobById(job.getJobConfigId());
        String jobName = distributionJobModel.map(DistributionJobModelData::getName).orElse(String.format("Unknown Job (%s)", job.getJobConfigId()));
        String channelName = distributionJobModel.map(DistributionJobModel::getChannelDescriptorName).orElse("Unknown Channel");
        String start = DateUtils.formatDateAsJsonString(DateUtils.fromInstantUTC(job.getStart()));
        String end = job.getEnd().map(instant -> DateUtils.formatDateAsJsonString(DateUtils.fromInstantUTC(instant))).orElse("");

        return new JobExecutionDiagnosticModel(
            jobName,
            channelName,
            start,
            end,
            job.getStatus(),
            job.getProcessedNotificationCount(),
            job.getTotalNotificationCount(),
            job.getRemainingEvents(),
            stageData
        );
    }

    private JobStageDiagnosticModel convertJobStageData(ExecutingJobStage executingJobStage) {
        String start = DateUtils.formatDateAsJsonString(DateUtils.fromInstantUTC(executingJobStage.getStart()));
        String end = executingJobStage.getEnd().map(instant -> DateUtils.formatDateAsJsonString(DateUtils.fromInstantUTC(instant))).orElse("");
        return new JobStageDiagnosticModel(
            executingJobStage.getStage(),
            start,
            end
        );
    }

    private String getJobName(UUID jobConfigId) {
        Optional<DistributionJobModel> distributionJobModel = jobAccessor.getJobById(jobConfigId);
        return distributionJobModel.map(DistributionJobModelData::getName).orElse(String.format("Unknown Job (%s)", jobConfigId));
    }

    private CompletedJobDiagnosticModel convertJobStatusData(JobCompletionStatusModel jobCompletionStatusModel) {
        JobCompletionStatusDurations durationsModel = jobCompletionStatusModel.getDurations();
        CompletedJobDurationDiagnosticModel durationDiagnosticModel = new CompletedJobDurationDiagnosticModel(
            DateUtils.formatDurationFromNanos(durationsModel.getJobDuration()),
            List.of(
                createJobStageDuration(JobStage.NOTIFICATION_PROCESSING, durationsModel.getNotificationProcessingDuration()),
                createJobStageDuration(JobStage.CHANNEL_PROCESSING, durationsModel.getChannelProcessingDuration()),
                createJobStageDuration(JobStage.ISSUE_CREATION, durationsModel.getIssueCreationDuration()),
                createJobStageDuration(JobStage.ISSUE_COMMENTING, durationsModel.getIssueCommentingDuration()),
                createJobStageDuration(JobStage.ISSUE_TRANSITION, durationsModel.getIssueTransitionDuration())
            )
        );

        String jobName = getJobName(jobCompletionStatusModel.getJobConfigId());
        return new CompletedJobDiagnosticModel(
            jobCompletionStatusModel.getJobConfigId(),
            jobName,
            jobCompletionStatusModel.getLatestNotificationCount(),
            jobCompletionStatusModel.getTotalNotificationCount(),
            jobCompletionStatusModel.getSuccessCount(),
            jobCompletionStatusModel.getFailureCount(),
            jobCompletionStatusModel.getLatestStatus(),
            DateUtils.formatDateAsJsonString(jobCompletionStatusModel.getFirstRun()),
            DateUtils.formatDateAsJsonString(jobCompletionStatusModel.getLastRun()),
            durationDiagnosticModel
        );

    }

    private CompletedJobStageDurationModel createJobStageDuration(JobStage jobStage, long nanosecondDuration) {
        return new CompletedJobStageDurationModel(jobStage.name(), DateUtils.formatDurationFromNanos(nanosecondDuration));
    }

}
