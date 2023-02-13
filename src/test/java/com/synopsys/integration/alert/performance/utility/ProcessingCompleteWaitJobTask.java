package com.synopsys.integration.alert.performance.utility;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.component.diagnostic.model.AlertQueueInformation;
import com.synopsys.integration.alert.component.diagnostic.model.CompletedJobDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.CompletedJobDurationDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.CompletedJobsDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.DiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.JobExecutionsDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.NotificationDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.NotificationTypeCount;
import com.synopsys.integration.alert.component.diagnostic.model.ProviderNotificationCounts;
import com.synopsys.integration.alert.component.diagnostic.model.RabbitMQDiagnosticModel;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.wait.WaitJobCondition;

public class ProcessingCompleteWaitJobTask implements WaitJobCondition {
    private static final String DIAGNOSTIC_ERROR_RESPONSE_MESSAGE = "Diagnostic unavailable";

    private final IntLogger intLogger;
    private final Gson gson;
    private final AlertRequestUtility alertRequestUtility;
    private final OffsetDateTime startSearchTime;
    private final int numberOfExpectedNotifications;
    private final Set<String> expectedJobIds;
    private final Long blackduckProviderConfigId;

    public ProcessingCompleteWaitJobTask(
        IntLogger intLogger,
        Gson gson,
        AlertRequestUtility alertRequestUtility,
        LocalDateTime startSearchTime,
        int numberOfExpectedNotifications,
        Set<String> expectedJobIds,
        Long blackduckProviderConfigId
    ) {
        this.intLogger = intLogger;
        this.gson = gson;
        this.alertRequestUtility = alertRequestUtility;
        this.startSearchTime = DateUtils.fromInstantUTC(startSearchTime.toInstant(ZoneOffset.UTC));
        this.numberOfExpectedNotifications = numberOfExpectedNotifications;
        this.expectedJobIds = expectedJobIds;
        this.blackduckProviderConfigId = blackduckProviderConfigId;
    }

    @Override
    public boolean isComplete() throws IntegrationException {
        return waitForNotificationToBeProcessedByAllJobs();
    }

    private boolean waitForNotificationToBeProcessedByAllJobs() throws IntegrationException {
        Optional<DiagnosticModel> diagnosticModelOptional = findDiagnosticsIfAvailable();
        if (diagnosticModelOptional.isEmpty()) {
            return false;
        }
        DiagnosticModel diagnosticModel = diagnosticModelOptional.get();
        logDiagnostics(diagnosticModel);

        CompletedJobsDiagnosticModel jobDiagnosticModel = diagnosticModel.getCompletedJobsDiagnosticModel();

        int totalNotifications = jobDiagnosticModel.getCompletedJobs()
            .stream()
            .filter(jobStatusDiagnosticModel -> expectedJobIds.contains(jobStatusDiagnosticModel.getJobConfigId().toString()))
            .filter(this::isAfterSearchTime)
            .map(CompletedJobDiagnosticModel::getTotalNotificationCount)
            .map(Long::intValue)
            .reduce(0, Integer::sum);

        List<NotificationTypeCount> notificationTypeCounts = diagnosticModel.getNotificationDiagnosticModel()
            .getProviderNotificationCounts()
            .stream()
            .filter(providerNotificationCounts -> providerNotificationCounts.getProviderConfigId() == blackduckProviderConfigId)
            .map(ProviderNotificationCounts::getNotificationCounts)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        long totalProviderNotificationCounts = notificationTypeCounts
            .stream()
            .filter(notificationTypeCount -> NotificationType.RULE_VIOLATION.equals(notificationTypeCount.getNotificationType()))
            .map(NotificationTypeCount::getCount)
            .reduce(0L, Long::sum);

        intLogger.info(String.format(
            "Performance: Found %s notifications, expected %s after time: %s. ",
            totalNotifications,
            totalProviderNotificationCounts,
            DateUtils.formatDateAsJsonString(startSearchTime)
        ));

        boolean expectedNotifications = totalNotifications >= numberOfExpectedNotifications;

        if (!expectedNotifications) {
            return false;
        }

        Set<String> jobIds = getJobIdsFromDiagnosticModel(diagnosticModel);

        intLogger.info(String.format("Performance: Job IDs discovered:  %s", jobIds.toString()));
        intLogger.info(String.format("Performance: Expected Job Ids:    %s", expectedJobIds.toString()));

        boolean expectedJobIdsDiscovered = expectedJobIds.size() == jobIds.size() && expectedJobIds.containsAll(jobIds);
        if (expectedJobIdsDiscovered) {
            boolean allQueuesEmpty = diagnosticModel.getRabbitMQDiagnosticModel().getQueues()
                .stream()
                .map(AlertQueueInformation::getMessageCount)
                .allMatch(count -> count == 0);

            boolean allExecutingJobsEmpty = diagnosticModel.getJobExecutionsDiagnosticModel().getJobExecutions().isEmpty();

            boolean allJobsHaveRun = jobDiagnosticModel.getCompletedJobs()
                .stream()
                .filter(jobStatusDiagnosticModel -> expectedJobIds.contains(jobStatusDiagnosticModel.getJobConfigId().toString()))
                .allMatch(this::isAfterSearchTime);
            intLogger.info(String.format("Performance: allJobsHaveRun: %s executingJobsEmpty: %s allQueuesEmpty: %s", allJobsHaveRun, allExecutingJobsEmpty, allQueuesEmpty));

            return allJobsHaveRun && allExecutingJobsEmpty && allQueuesEmpty;
        }
        return false;
    }

    private boolean isAfterSearchTime(CompletedJobDiagnosticModel jobStatusDiagnosticModel) {
        try {
            return DateUtils.parseDateFromJsonString(jobStatusDiagnosticModel.getLastRun()).isAfter(startSearchTime);
        } catch (ParseException e) {
            return false;
        }
    }

    private Set<String> getJobIdsFromDiagnosticModel(DiagnosticModel diagnosticModel) {
        return diagnosticModel.getCompletedJobsDiagnosticModel().getCompletedJobs()
            .stream()
            .map(CompletedJobDiagnosticModel::getJobConfigId)
            .map(UUID::toString)
            .collect(Collectors.toSet());
    }

    private Optional<DiagnosticModel> findDiagnosticsIfAvailable() throws IntegrationException {
        DiagnosticModel diagnosticModel;
        try {
            String diagnosticResponse = alertRequestUtility.executeGetRequest("/api/diagnostic", DIAGNOSTIC_ERROR_RESPONSE_MESSAGE);
            diagnosticModel = gson.fromJson(diagnosticResponse, DiagnosticModel.class);
        } catch (IntegrationRestException e) {
            return Optional.empty();
        }
        return Optional.ofNullable(diagnosticModel);
    }

    private void logDiagnostics(DiagnosticModel diagnosticModel) {
        NotificationDiagnosticModel notificationDiagnosticModel = diagnosticModel.getNotificationDiagnosticModel();
        RabbitMQDiagnosticModel rabbitMQDiagnosticModel = diagnosticModel.getRabbitMQDiagnosticModel();
        CompletedJobsDiagnosticModel completedJobsDiagnosticModel = diagnosticModel.getCompletedJobsDiagnosticModel();
        JobExecutionsDiagnosticModel executingJobsModel = diagnosticModel.getJobExecutionsDiagnosticModel();

        intLogger.info(String.format("Diagnostic Info: %s", diagnosticModel.getRequestTimestamp()));
        intLogger.info("Performance: Notification Diagnostics");
        intLogger.info(String.format("Total # Notifications: %s", notificationDiagnosticModel.getNumberOfNotifications()));
        intLogger.trace("Performance: RabbitMQ Diagnostics");
        rabbitMQDiagnosticModel.getQueues().forEach(queueInformation ->
            intLogger.trace(String.format("Queue Name: %s, Message Count: %s", queueInformation.getName(), queueInformation.getMessageCount()))
        );
        intLogger.info("Performance: Job Diagnostics");
        completedJobsDiagnosticModel.getCompletedJobs()
            .forEach(jobStatus -> {
                intLogger.info(String.format(
                    "Job: %s, last status: %s, last run: %s, notifications: (latest: %d total: %d), iterations: (SUCCESS: %s FAILURE: %s)",
                    jobStatus.getJobName(),
                    jobStatus.getLatestStatus(),
                    jobStatus.getLastRun(),
                    jobStatus.getLatestNotificationCount(),
                    jobStatus.getTotalNotificationCount(),
                    jobStatus.getSuccessCount(),
                    jobStatus.getFailureCount()
                ));
                CompletedJobDurationDiagnosticModel jobDurationModel = jobStatus.getDurations();
                intLogger.info(String.format("Job duration: %s", jobDurationModel.getJobDuration()));
                jobDurationModel.getStageDurations().forEach(stage ->
                    intLogger.info(String.format("    %s: %s", stage.getName(), stage.getDuration())));
            });
        intLogger.info("Performance: Job Diagnostics");
        intLogger.info("Performance: Executing Job Diagnostics");
        executingJobsModel.getJobExecutions()
            .forEach(execution -> {
                intLogger.info(String.format(
                    "Job: %s, status: %s, notifications: (%d of %d), channel: %s, start: %s, end: %s",
                    execution.getJobName(),
                    execution.getStatus(),
                    execution.getProcessedNotificationCount(),
                    execution.getTotalNotificationCount(),
                    execution.getChannelName(),
                    execution.getStart(),
                    execution.getEnd()
                ));
                execution.getStages()
                    .forEach(stage -> intLogger.info(String.format("    Stage: %s, start: %s, end: %s", stage.getStage(), stage.getStart(), stage.getEnd())));
            });
        intLogger.info("Performance: Executing Job Diagnostics");

        completedJobsDiagnosticModel.getCompletedJobs()
            .stream()
            .filter(jobDiagnostics -> expectedJobIds.contains(jobDiagnostics.getJobConfigId().toString()))
            .forEach(jobDiagnostics -> {
                intLogger.info(String.format(
                    "Job: %s average time: %s", jobDiagnostics.getJobName(), jobDiagnostics.getDurations().getJobDuration()));
            });
    }
}
