package com.synopsys.integration.alert.component.diagnostic.model;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.api.common.model.Obfuscated;

public class DiagnosticModel extends AlertSerializableModel implements Obfuscated<DiagnosticModel> {
    private static final long serialVersionUID = 6714869824373312126L;
    private static final String NO_AUDIT_CONTENT_MESSAGE = "No audit content found";

    private Long numberOfNotifications;
    private Long numberOfNotificationsProcessed;
    private Long numberOfNotificationsUnprocessed;
    private Long numberOfAuditEntriesSuccessful;
    private Long numberOfAuditEntriesFailed;
    private Long numberOfAuditEntriesPending;
    @Nullable
    private String averageAuditTime;

    private String requestTimestamp;

    public DiagnosticModel() {
        // For serialization
    }

    public DiagnosticModel(
        Long numberOfNotifications,
        Long numberOfNotificationsProcessed,
        Long numberOfNotificationsUnprocessed,
        Long numberOfAuditEntriesSuccessful,
        Long numberOfAuditEntriesFailed,
        Long numberOfAuditEntriesPending,
        String requestTimestamp,
        @Nullable String averageAuditTime
    ) {
        this.numberOfNotifications = numberOfNotifications;
        this.numberOfNotificationsProcessed = numberOfNotificationsProcessed;
        this.numberOfNotificationsUnprocessed = numberOfNotificationsUnprocessed;
        this.numberOfAuditEntriesSuccessful = numberOfAuditEntriesSuccessful;
        this.numberOfAuditEntriesFailed = numberOfAuditEntriesFailed;
        this.numberOfAuditEntriesPending = numberOfAuditEntriesPending;
        this.requestTimestamp = requestTimestamp;
        this.averageAuditTime = averageAuditTime;
    }

    public Long getNumberOfNotifications() {
        return numberOfNotifications;
    }

    public Long getNumberOfNotificationsProcessed() {
        return numberOfNotificationsProcessed;
    }

    public Long getNumberOfNotificationsUnprocessed() {
        return numberOfNotificationsUnprocessed;
    }

    public Long getNumberOfAuditEntriesSuccessful() {
        return numberOfAuditEntriesSuccessful;
    }

    public Long getNumberOfAuditEntriesFailed() {
        return numberOfAuditEntriesFailed;
    }

    public Long getNumberOfAuditEntriesPending() {
        return numberOfAuditEntriesPending;
    }

    public String getRequestTimestamp() {
        return requestTimestamp;
    }

    public Optional<String> getAverageAuditTime() {
        return Optional.ofNullable(averageAuditTime);
    }

    @Override
    public DiagnosticModel obfuscate() {
        // Diagnostic model does not handle sensitive data and therefore does not need any fields to be obfuscated
        return new DiagnosticModel(
            numberOfNotifications,
            numberOfNotificationsProcessed,
            numberOfNotificationsUnprocessed,
            numberOfAuditEntriesSuccessful,
            numberOfAuditEntriesFailed,
            numberOfAuditEntriesPending,
            requestTimestamp,
            getAverageAuditTime().orElse(NO_AUDIT_CONTENT_MESSAGE)
        );
    }
}
