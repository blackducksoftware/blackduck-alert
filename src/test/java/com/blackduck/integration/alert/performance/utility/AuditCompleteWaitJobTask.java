package com.blackduck.integration.alert.performance.utility;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.blackduck.integration.alert.common.enumeration.AuditEntryStatus;
import com.blackduck.integration.alert.common.persistence.model.AuditEntryModel;
import com.blackduck.integration.alert.common.persistence.model.AuditEntryPageModel;
import com.blackduck.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.blackduck.integration.alert.common.rest.model.JobAuditModel;
import com.blackduck.integration.alert.common.rest.model.NotificationConfig;
import com.blackduck.integration.alert.common.util.DateUtils;
import com.blackduck.integration.alert.component.diagnostic.model.AlertQueueInformation;
import com.blackduck.integration.alert.component.diagnostic.model.AuditDiagnosticModel;
import com.blackduck.integration.alert.component.diagnostic.model.DiagnosticModel;
import com.blackduck.integration.alert.component.diagnostic.model.NotificationDiagnosticModel;
import com.blackduck.integration.alert.component.diagnostic.model.RabbitMQDiagnosticModel;
import com.blackduck.integration.blackduck.api.manual.component.VulnerabilityNotificationContent;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.rest.exception.IntegrationRestException;
import com.blackduck.integration.wait.WaitJobCondition;

public class AuditCompleteWaitJobTask implements WaitJobCondition {
    private static final String AUDIT_ERROR_RESPONSE_MESSAGE = "Could not get the Alert audit entries.";
    private static final String DIAGNOSTIC_ERROR_RESPONSE_MESSAGE = "Diagnostic unavailable";

    private final IntLogger intLogger;
    private final DateTimeFormatter dateTimeFormatter;
    private final Gson gson;
    private final AlertRequestUtility alertRequestUtility;
    private final LocalDateTime startSearchTime;
    private final int numberOfExpectedNotifications;
    private final NotificationType notificationType;
    private final Set<String> expectedJobIds;

    public AuditCompleteWaitJobTask(
        IntLogger intLogger,
        DateTimeFormatter dateTimeFormatter,
        Gson gson,
        AlertRequestUtility alertRequestUtility,
        LocalDateTime startSearchTime,
        int numberOfExpectedNotifications,
        NotificationType notificationType,
        Set<String> expectedJobIds
    ) {
        this.intLogger = intLogger;
        this.dateTimeFormatter = dateTimeFormatter;
        this.gson = gson;
        this.alertRequestUtility = alertRequestUtility;
        this.startSearchTime = startSearchTime;
        this.numberOfExpectedNotifications = numberOfExpectedNotifications;
        this.notificationType = notificationType;
        this.expectedJobIds = expectedJobIds;
    }

    @Override
    public boolean isComplete() throws IntegrationException {
        return waitForNotificationToBeProcessedByAllJobs();
    }

    private boolean waitForNotificationToBeProcessedByAllJobs() throws IntegrationException {
        Optional<DiagnosticModel> diagnosticModel = findDiagnosticsIfAvailable();
        diagnosticModel.ifPresent(this::logDiagnostics);

        String allNotificationsCreatedResponse = alertRequestUtility.executeGetRequest(
            createAuditRequestString(0, 1, notificationType),
            AUDIT_ERROR_RESPONSE_MESSAGE
        );
        AuditEntryPageModel totalNotificationsCreatedPageModel = gson.fromJson(allNotificationsCreatedResponse, AuditEntryPageModel.class);
        if (totalNotificationsCreatedPageModel.getTotalPages() < numberOfExpectedNotifications) {
            intLogger.info(String.format("Performance: Found %s audit entries, expected %s. ", totalNotificationsCreatedPageModel.getTotalPages(), numberOfExpectedNotifications));
            return false;
        }
        intLogger.info(String.format("Performance: Found %s audit entries, expected %s. ", totalNotificationsCreatedPageModel.getTotalPages(), numberOfExpectedNotifications));

        Set<String> jobIds = getJobIdsFromAuditEntries();

        intLogger.info(String.format("Performance: Job IDs discovered in audit: %s", jobIds.toString()));
        intLogger.info(String.format("Performance: Expected Job Ids:            %s", expectedJobIds.toString()));

        boolean expectedJobIdsDiscovered = expectedJobIds.size() == jobIds.size() && expectedJobIds.containsAll(jobIds);
        if (expectedJobIdsDiscovered) {
            boolean allQueuesEmpty = diagnosticModel
                .map(DiagnosticModel::getRabbitMQDiagnosticModel)
                .map(RabbitMQDiagnosticModel::getQueues)
                .stream()
                .flatMap(List::stream)
                .map(AlertQueueInformation::getMessageCount)
                .allMatch(count -> count == 0);

            if (diagnosticModel.isEmpty() || allQueuesEmpty) {
                logAverageAuditTime();
                return true;
            } else {
                return false;
            }

        }
        return false;
    }

    private Set<String> getJobIdsFromAuditEntries() throws IntegrationException {
        Set<String> jobIds = new HashSet<>();

        int pageNumber = 0;
        AuditEntryPageModel auditEntryPageModel = getPageOfAuditEntries(pageNumber, 100);
        do {
            List<JobAuditModel> jobAuditModels = auditEntryPageModel.getContent().stream()
                .filter(auditEntryModel -> isNotificationAfterTime(startSearchTime, auditEntryModel.getNotification()))
                .filter(auditEntryModel -> notificationType.name().equals(auditEntryModel.getNotification().getNotificationType()))
                .filter(auditEntryModel -> isNotificationAllowed(auditEntryModel.getNotification()))
                .map(AuditEntryModel::getJobs)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

            intLogger.debug(String.format("Performance: Found %s audit entries discovered. ", auditEntryPageModel.getContent().size()));
            boolean anyPending = jobAuditModels.stream()
                .anyMatch(Predicate.not(this::jobFinished));
            if (anyPending) {
                intLogger.info("Performance: Some audit entries are still processing. Continuing...");
                return Set.of();
            }

            jobIds.addAll(jobAuditModels.stream()
                .map(JobAuditModel::getConfigId)
                .collect(Collectors.toSet()));

            pageNumber++;
            auditEntryPageModel = getPageOfAuditEntries(pageNumber, 100);
        } while (auditEntryPageModel.getCurrentPage() < auditEntryPageModel.getTotalPages());

        return jobIds;
    }

    private AuditEntryPageModel getPageOfAuditEntries(int pageNumber, int pageSize) throws IntegrationException {
        String auditEntryPageRequest = createAuditRequestString(pageNumber, pageSize, notificationType);
        String response = alertRequestUtility.executeGetRequest(
            auditEntryPageRequest,
            AUDIT_ERROR_RESPONSE_MESSAGE
        );
        return gson.fromJson(response, AuditEntryPageModel.class);
    }

    private boolean isNotificationAfterTime(LocalDateTime startSearchTime, NotificationConfig notificationConfig) {
        String createdAt = notificationConfig.getCreatedAt();
        LocalDateTime createdAtTime = LocalDateTime.parse(createdAt, dateTimeFormatter);
        return createdAtTime.isAfter(startSearchTime);
    }

    private boolean isNotificationAllowed(NotificationConfig notificationConfig) {
        // Vulnerability requires additional filtering due to the contents of the notification
        if (notificationType != NotificationType.VULNERABILITY) {
            return true;
        }
        JsonObject jsonObject = gson.fromJson(notificationConfig.getContent(), JsonObject.class);
        JsonElement content = jsonObject.get("content");
        VulnerabilityNotificationContent notification = gson.fromJson(content, VulnerabilityNotificationContent.class);
        return notification.getNewVulnerabilityCount() > 0;
    }

    private boolean jobFinished(JobAuditModel jobAuditModel) {
        if (AuditEntryStatus.SUCCESS.getDisplayName().equals(jobAuditModel.getAuditJobStatusModel().getStatus())) {
            return true;
        }
        if (AuditEntryStatus.FAILURE.getDisplayName().equals(jobAuditModel.getAuditJobStatusModel().getStatus())) {
            intLogger.error(String.format("Audit job discovered with errors: %s", jobAuditModel.getName()));
            intLogger.error(jobAuditModel.getErrorMessage());
            return true;
        }
        return false;
    }

    private String createAuditRequestString(int pageNumber, int pageSize, NotificationType notificationType) {
        return String.format(
            "/api/audit?pageNumber=%s&pageSize=%s&searchTerm=%s&sortField=createdAt&sortOrder=desc&onlyShowSentNotifications=false",
            pageNumber,
            pageSize,
            URLEncoder.encode(String.format("\"%s\"", notificationType.name()), StandardCharsets.UTF_8)
        );
    }

    private void logAverageAuditTime() throws IntegrationException {
        List<AuditJobStatusModel> auditJobStatusModels = new ArrayList<>();
        int pageNumber = 0;
        AuditEntryPageModel auditEntryPageModel = getPageOfAuditEntries(pageNumber, 100);
        do {
            auditJobStatusModels.addAll(auditEntryPageModel.getContent().stream()
                .map(AuditEntryModel::getJobs)
                .flatMap(List::stream)
                .map(JobAuditModel::getAuditJobStatusModel)
                .collect(Collectors.toList()));
            pageNumber++;
            auditEntryPageModel = getPageOfAuditEntries(pageNumber, 100);
        } while (auditEntryPageModel.getCurrentPage() < auditEntryPageModel.getTotalPages());

        OptionalDouble averageAuditTimeSeconds = auditJobStatusModels.stream()
            .map(this::calculateAuditDuration)
            .flatMap(Optional::stream)
            .map(Duration::toSeconds)
            .mapToLong(Long::valueOf)
            .average();

        if (averageAuditTimeSeconds.isEmpty()) {
            intLogger.info("Performance: Could not calculate average audit time.");
            return;
        }
        Duration duration = Duration.ofSeconds(Double.valueOf(averageAuditTimeSeconds.getAsDouble()).longValue());
        String durationFormatted = String.format("%sH:%sm:%ss", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
        intLogger.info(String.format("Performance: Average audit time: %s", durationFormatted));
    }

    private Optional<Duration> calculateAuditDuration(AuditJobStatusModel auditJobStatusModel) {
        String timeSent = auditJobStatusModel.getTimeLastSent();
        if (timeSent == null) {
            intLogger.error(String.format("Could not find timeSent for job: %s", auditJobStatusModel.getJobId()));
            return Optional.empty();
        }
        try {
            OffsetDateTime timeCreated = DateUtils.parseDate(auditJobStatusModel.getTimeAuditCreated(), DateUtils.AUDIT_DATE_FORMAT);
            OffsetDateTime timeLastSent = DateUtils.parseDate(timeSent, DateUtils.AUDIT_DATE_FORMAT);
            return Optional.of(Duration.between(timeCreated, timeLastSent));
        } catch (ParseException e) {
            intLogger.error(e.toString());
        }
        return Optional.empty();
    }

    private Optional<DiagnosticModel> findDiagnosticsIfAvailable() throws IntegrationException {
        // Diagnostic information is unavailable prior to 6.11.0 Alert. When testing against external Alert servers prior to 6.11.0 this will ignore printing Diagnostic info.
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

        intLogger.info(String.format("Diagnostic Info: %s", diagnosticModel.getRequestTimestamp()));
        intLogger.info("Performance: Notification Diagnostics");
        intLogger.info(String.format("Total # Notifications: %s", notificationDiagnosticModel.getNumberOfNotifications()));
        intLogger.info(String.format("Notifications Processed: %s", notificationDiagnosticModel.getNumberOfNotificationsProcessed()));
        intLogger.info(String.format("Notifications Unprocessed: %s", notificationDiagnosticModel.getNumberOfNotificationsUnprocessed()));
        intLogger.info("Performance: RabbitMQ Diagnostics");
        for (AlertQueueInformation queueInformation : rabbitMQDiagnosticModel.getQueues()) {
            intLogger.info(String.format("Queue Name: %s, Message Count: %s", queueInformation.getName(), queueInformation.getMessageCount()));

        }
        intLogger.info("Performance: RabbitMQ Diagnostics");
        intLogger.info("Performance: Audit Diagnostics");
        intLogger.info(String.format("Audit Entries Successful: %s", auditDiagnosticModel.getNumberOfAuditEntriesSuccessful()));
        intLogger.info(String.format("Audit Entries Failed: %s", auditDiagnosticModel.getNumberOfAuditEntriesFailed()));
        intLogger.info(String.format("Audit Entries Pending: %s", auditDiagnosticModel.getNumberOfAuditEntriesPending()));
        intLogger.info("Performance: Audit Diagnostics");

        auditDiagnosticModel.getAverageAuditTime().ifPresent(auditTime -> intLogger.info(String.format("Average audit time: %s", auditTime)));
    }
}
