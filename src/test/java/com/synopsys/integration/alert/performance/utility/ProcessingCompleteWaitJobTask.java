package com.synopsys.integration.alert.performance.utility;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.component.diagnostic.model.AlertQueueInformation;
import com.synopsys.integration.alert.component.diagnostic.model.AuditDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.CompletedJobDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.CompletedJobDurationDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.CompletedJobsDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.DiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.JobExecutionsDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.NotificationDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.RabbitMQDiagnosticModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.wait.WaitJobCondition;

public class ProcessingCompleteWaitJobTask implements WaitJobCondition {
    private static final String DIAGNOSTIC_ERROR_RESPONSE_MESSAGE = "Diagnostic unavailable";

    private final IntLogger intLogger;
    private final Gson gson;
    private final AlertRequestUtility alertRequestUtility;
    private final LocalDateTime startSearchTime;
    private final int numberOfExpectedNotifications;
    private final Set<String> expectedJobIds;

    public ProcessingCompleteWaitJobTask(
        IntLogger intLogger,
        Gson gson,
        AlertRequestUtility alertRequestUtility,
        LocalDateTime startSearchTime,
        int numberOfExpectedNotifications,
        Set<String> expectedJobIds
    ) {
        this.intLogger = intLogger;
        this.gson = gson;
        this.alertRequestUtility = alertRequestUtility;
        this.startSearchTime = startSearchTime;
        this.numberOfExpectedNotifications = numberOfExpectedNotifications;
        this.expectedJobIds = expectedJobIds;
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

            CompletedJobsDiagnosticModel jobDiagnosticModel = diagnosticModel.getCompletedJobsDiagnosticModel();

            int totalNotifications = jobDiagnosticModel.getCompletedJobs()
                .stream()
                .filter(jobStatusDiagnosticModel -> expectedJobIds.contains(jobStatusDiagnosticModel.getJobConfigId().toString()))
                .filter(this::isAfterSearchTime)
                .map(CompletedJobDiagnosticModel::getTotalNotificationCount)
                .map(Long::intValue)
                .reduce(0, Integer::sum);

            intLogger.info(String.format("Performance: Found %s notifications, expected %s. ", totalNotifications, numberOfExpectedNotifications));
            boolean allJobsHaveRun = jobDiagnosticModel.getCompletedJobs()
                .stream()
                .filter(jobStatusDiagnosticModel -> expectedJobIds.contains(jobStatusDiagnosticModel.getJobConfigId().toString()))
                .allMatch(this::isAfterSearchTime);

            boolean expectedNotifications = totalNotifications >= numberOfExpectedNotifications;

            return expectedNotifications && allJobsHaveRun && allExecutingJobsEmpty && allQueuesEmpty;
        }
        return false;
    }

    private boolean isAfterSearchTime(CompletedJobDiagnosticModel jobStatusDiagnosticModel) {
        try {
            return DateUtils.parseDateFromJsonString(jobStatusDiagnosticModel.getLastRun()).isAfter(DateUtils.fromInstantUTC(startSearchTime.toInstant(
                ZoneOffset.UTC)));
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
        AuditDiagnosticModel auditDiagnosticModel = diagnosticModel.getAuditDiagnosticModel();
        RabbitMQDiagnosticModel rabbitMQDiagnosticModel = diagnosticModel.getRabbitMQDiagnosticModel();
        CompletedJobsDiagnosticModel completedJobsDiagnosticModel = diagnosticModel.getCompletedJobsDiagnosticModel();
        JobExecutionsDiagnosticModel executingJobsModel = diagnosticModel.getJobExecutionsDiagnosticModel();

        intLogger.info(String.format("Diagnostic Info: %s", diagnosticModel.getRequestTimestamp()));
        intLogger.info("Performance: Notification Diagnostics");
        intLogger.info(String.format("Total # Notifications: %s", notificationDiagnosticModel.getNumberOfNotifications()));
        intLogger.info("Performance: RabbitMQ Diagnostics");
        rabbitMQDiagnosticModel.getQueues().forEach(queueInformation ->
            intLogger.info(String.format("Queue Name: %s, Message Count: %s", queueInformation.getName(), queueInformation.getMessageCount()))
        );
        intLogger.info("Performance: RabbitMQ Diagnostics");
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

        auditDiagnosticModel.getAverageAuditTime().ifPresent(auditTime -> intLogger.info(String.format("Average audit time: %s", auditTime)));
    }
}
