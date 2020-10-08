package com.synopsys.integration.alert.performance.utility;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import com.synopsys.integration.wait.WaitJobTask;

public class NotificationWaitJobTask implements WaitJobTask {
    private final IntLogger intLogger;
    private final DateTimeFormatter dateTimeFormatter;
    private final Gson gson;
    private final AlertRequestUtility alertRequestUtility;

    private final LocalDateTime startSearchTime;
    private final List<String> jobIdsToMatch;

    public NotificationWaitJobTask(IntLogger intLogger, DateTimeFormatter dateTimeFormatter, Gson gson, AlertRequestUtility alertRequestUtility, LocalDateTime startSearchTime, String jobIdToMatch) {
        this(intLogger, dateTimeFormatter, gson, alertRequestUtility, startSearchTime, List.of(jobIdToMatch));
    }

    public NotificationWaitJobTask(IntLogger intLogger, DateTimeFormatter dateTimeFormatter, Gson gson, AlertRequestUtility alertRequestUtility, LocalDateTime startSearchTime, List<String> jobIdsToMatch) {
        this.intLogger = intLogger;
        this.dateTimeFormatter = dateTimeFormatter;
        this.gson = gson;
        this.alertRequestUtility = alertRequestUtility;
        this.startSearchTime = startSearchTime;
        this.jobIdsToMatch = jobIdsToMatch;
    }

    @Override
    public boolean isComplete() throws IntegrationException {
        return waitForNotificationToBeProcessedByAllJobs();
    }

    private boolean waitForNotificationToBeProcessedByAllJobs() throws IntegrationException {
        String response = alertRequestUtility.executeGetRequest("/api/audit?pageNumber=0&pageSize=2&searchTerm=VULNERABILITY&sortField=createdAt&sortOrder=desc&onlyShowSentNotifications=false", "Could not get the Alert audit entries.");
        AuditEntryPageModel auditEntryPageModel = gson.fromJson(response, AuditEntryPageModel.class);
        Optional<AuditEntryModel> matchingAuditEntry = auditEntryPageModel.getContent().stream()
                                                           .filter(auditEntryModel -> isNotificationAfterTime(startSearchTime, auditEntryModel.getNotification()))
                                                           .filter(auditEntryModel -> NotificationType.VULNERABILITY.name().equals(auditEntryModel.getNotification().getNotificationType()))
                                                           .filter(auditEntryModel -> isNotificationForNewVulnerabilities(auditEntryModel.getNotification()))
                                                           .findFirst();
        if (matchingAuditEntry.isPresent()) {
            AuditEntryModel auditEntryModel = matchingAuditEntry.get();
            intLogger.info(String.format("The notification has been processed by %s jobs.", auditEntryModel.getJobs().size()));
            return haveAllJobsSuccessfullyProcessed(auditEntryModel.getJobs());
        }
        return false;
    }

    private boolean isNotificationAfterTime(LocalDateTime startSearchTime, NotificationConfig notificationConfig) {
        String createdAt = notificationConfig.getCreatedAt();
        LocalDateTime createdAtTime = LocalDateTime.parse(createdAt, dateTimeFormatter);
        return createdAtTime.isAfter(startSearchTime);
    }

    private boolean isNotificationForNewVulnerabilities(NotificationConfig notificationConfig) {
        JsonObject jsonObject = gson.fromJson(notificationConfig.getContent(), JsonObject.class);
        JsonElement content = jsonObject.get("content");
        VulnerabilityNotificationContent notification = gson.fromJson(content, VulnerabilityNotificationContent.class);
        notification.getNewVulnerabilityCount();
        return notification.getNewVulnerabilityCount() > 0;
    }

    private boolean haveAllJobsSuccessfullyProcessed(List<JobAuditModel> auditJobs) {
        if (auditJobs.size() != jobIdsToMatch.size()) {
            return false;
        }
        boolean allJobsSuccessful = auditJobs.stream()
                                        .allMatch(jobAuditModel -> AuditEntryStatus.SUCCESS.getDisplayName().equals(jobAuditModel.getAuditJobStatusModel().getStatus()));
        List<String> remainingJobs = new ArrayList<>(jobIdsToMatch);
        auditJobs.stream()
            .forEach(jobAuditModel -> remainingJobs.remove(jobAuditModel.getConfigId()));

        if (allJobsSuccessful && remainingJobs.isEmpty()) {
            return true;
        }
        return false;
    }
}
