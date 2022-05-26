package com.synopsys.integration.alert.performance.utility;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryModel;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryPageModel;
import com.synopsys.integration.alert.common.rest.model.JobAuditModel;
import com.synopsys.integration.alert.common.rest.model.NotificationConfig;
import com.synopsys.integration.blackduck.api.manual.component.VulnerabilityNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.wait.WaitJobCondition;

public class NotificationWaitJobTaskV2 implements WaitJobCondition {
    private static final String AUDIT_ERROR_RESPONSE_MESSAGE = "Could not get the Alert audit entries.";

    private final IntLogger intLogger;
    private final DateTimeFormatter dateTimeFormatter;
    private final Gson gson;
    private final AlertRequestUtility alertRequestUtility;
    private final LocalDateTime startSearchTime;
    private final int numberOfExpectedNotifications;
    private final NotificationType notificationType;
    private final Set<String> expectedJobIds;

    public NotificationWaitJobTaskV2(
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
        return true;
        //        Set<String> jobIds = getJobIdsFromAuditEntries();
        //
        //        intLogger.info(String.format("Performance: Job IDs discovered in audit: %s", jobIds.toString()));
        //        intLogger.info(String.format("Performance: Expected Job Ids:            %s", expectedJobIds.toString()));
        //        return expectedJobIds.size() == jobIds.size() && expectedJobIds.containsAll(jobIds);
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
            // TODO add this check when we want to validate sending to the channel as well.
            //            boolean anyPending = jobAuditModels.stream()
            //                .anyMatch(Predicate.not(this::jobFinished));
            //            if (anyPending) {
            //                intLogger.info("Performance: Some audit entries are still processing. Continuing...");
            //                return Set.of();
            //            }

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
        return AuditEntryStatus.SUCCESS.getDisplayName().equals(jobAuditModel.getAuditJobStatusModel().getStatus()) ||
            AuditEntryStatus.FAILURE.getDisplayName().equals(jobAuditModel.getAuditJobStatusModel().getStatus());
    }

    private String createAuditRequestString(int pageNumber, int pageSize, NotificationType notificationType) {
        return String.format(
            "/api/audit?pageNumber=%s&pageSize=%s&searchTerm=%s&sortField=createdAt&sortOrder=desc&onlyShowSentNotifications=false",
            pageNumber,
            pageSize,
            URLEncoder.encode(String.format("\"%s\"", notificationType.name()), StandardCharsets.UTF_8)
        );
    }
}
