/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.diagnostic.database;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.api.distribution.execution.AggregatedExecutionResults;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJob;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobStage;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.persistence.accessor.DiagnosticAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModelData;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.component.diagnostic.model.AuditDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.DiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.JobExecutionDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.JobExecutionsDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.JobStageDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.NotificationDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.RabbitMQDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.SystemDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.utility.RabbitMQDiagnosticUtility;
import com.synopsys.integration.alert.database.api.StaticJobAccessor;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.notification.NotificationContentRepository;

@Component
public class DefaultDiagnosticAccessor implements DiagnosticAccessor {
    private final NotificationContentRepository notificationContentRepository;
    private final AuditEntryRepository auditEntryRepository;
    private final RabbitMQDiagnosticUtility rabbitMQDiagnosticUtility;

    private final StaticJobAccessor jobAccessor;
    private final ExecutingJobManager executingJobManager;

    @Autowired
    public DefaultDiagnosticAccessor(
        NotificationContentRepository notificationContentRepository,
        AuditEntryRepository auditEntryRepository,
        RabbitMQDiagnosticUtility rabbitMQDiagnosticUtility,
        StaticJobAccessor staticJobAccessor,
        ExecutingJobManager executingJobManager
    ) {
        this.notificationContentRepository = notificationContentRepository;
        this.auditEntryRepository = auditEntryRepository;
        this.rabbitMQDiagnosticUtility = rabbitMQDiagnosticUtility;
        this.jobAccessor = staticJobAccessor;
        this.executingJobManager = executingJobManager;
    }

    @Override
    @Transactional(readOnly = true)
    public DiagnosticModel getDiagnosticInfo() {
        NotificationDiagnosticModel notificationDiagnosticModel = getNotificationDiagnosticInfo();
        AuditDiagnosticModel auditDiagnosticModel = getAuditDiagnosticInfo();
        SystemDiagnosticModel systemDiagnosticModel = getSystemInfo();
        RabbitMQDiagnosticModel rabbitMQDiagnosticModel = rabbitMQDiagnosticUtility.getRabbitMQDiagnostics();
        JobExecutionsDiagnosticModel jobExecutionsDiagnosticModel = getJobDiagnosticModel();
        return new DiagnosticModel(
            LocalDateTime.now().toString(),
            notificationDiagnosticModel,
            auditDiagnosticModel,
            systemDiagnosticModel,
            rabbitMQDiagnosticModel,
            jobExecutionsDiagnosticModel
        );
    }

    private NotificationDiagnosticModel getNotificationDiagnosticInfo() {
        long numberOfNotifications = notificationContentRepository.count();
        long numberOfNotificationsProcessed = notificationContentRepository.countByProcessed(true);
        long numberOfNotificationsUnprocessed = notificationContentRepository.countByProcessed(false);
        return new NotificationDiagnosticModel(numberOfNotifications, numberOfNotificationsProcessed, numberOfNotificationsUnprocessed);
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

    private JobExecutionsDiagnosticModel getJobDiagnosticModel() {
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

    private List<JobExecutionDiagnosticModel> getExecutionData() {
        List<JobExecutionDiagnosticModel> jobExecutions = new LinkedList<>();
        int pageSize = 100;
        int pageNumber = 1;
        AlertPagedModel<ExecutingJob> page = executingJobManager.getExecutingJobs(pageNumber, pageSize);
        while (page.getCurrentPage() <= page.getTotalPages()) {
            jobExecutions.addAll(page.getModels().stream()
                .map(this::convertExecutionData)
                .collect(Collectors.toList()));
            pageNumber++;
            page = executingJobManager.getExecutingJobs(pageNumber, pageSize);
        }

        return jobExecutions;
    }

    private JobExecutionDiagnosticModel convertExecutionData(ExecutingJob job) {
        List<JobStageDiagnosticModel> stageData = job.getStages().values()
            .stream()
            .map(this::convertJobStageData)
            .collect(Collectors.toList());
        Optional<DistributionJobModel> distributionJobModel = jobAccessor.getJobById(job.getJobConfigId());
        String jobName = distributionJobModel.map(DistributionJobModelData::getName).orElse(String.format("Unknown Job (%s)", job.getJobConfigId()));
        String channelName = distributionJobModel.map(DistributionJobModel::getChannelDescriptorName).orElse("Unknown Channel");
        String start = DateUtils.formatDateAsJsonString(DateUtils.fromInstantUTC(job.getStart()));
        String end = job.getEnd().map(instant -> DateUtils.formatDateAsJsonString(DateUtils.fromInstantUTC(instant))).orElse("");

        return new JobExecutionDiagnosticModel(jobName, channelName, start, end, job.getStatus(), job.getNotificationCount(), stageData);
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

}
