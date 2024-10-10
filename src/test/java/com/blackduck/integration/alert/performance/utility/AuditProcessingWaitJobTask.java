/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.performance.utility;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import com.blackduck.integration.alert.common.persistence.model.AuditEntryPageModel;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.wait.WaitJobCondition;
import com.google.gson.Gson;

//TODO: Refactor the duplicate code between this WaitJobCondition and AuditCompleteWaitJobTask
public class AuditProcessingWaitJobTask implements WaitJobCondition {
    private static final String AUDIT_ERROR_RESPONSE_MESSAGE = "Could not get the Alert audit entries.";

    private final IntLogger intLogger;
    private final DateTimeFormatter dateTimeFormatter;
    private final Gson gson;
    private final AlertRequestUtility alertRequestUtility;
    private final LocalDateTime startSearchTime;
    private final int numberOfExpectedNotifications;
    private final NotificationType notificationType;
    private final Set<String> expectedJobIds;

    public AuditProcessingWaitJobTask(
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
