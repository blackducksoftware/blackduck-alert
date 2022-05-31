package com.synopsys.integration.alert.component.diagnostic.model;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.api.common.model.Obfuscated;

public class DiagnosticModel extends AlertSerializableModel implements Obfuscated<DiagnosticModel> {
    private static final long serialVersionUID = 6714869824373312126L;
    private Long numberOfNotifications;
    private Long numberOfNotificationsProcessed;
    private Long numberOfNotificationsUnprocessed;

    private String requestTimestamp;

    public DiagnosticModel() {
        // For serialization
    }

    public DiagnosticModel(
        Long numberOfNotifications,
        Long numberOfNotificationsProcessed,
        Long numberOfNotificationsUnprocessed,
        String requestTimestamp
    ) {
        this.numberOfNotifications = numberOfNotifications;
        this.numberOfNotificationsProcessed = numberOfNotificationsProcessed;
        this.numberOfNotificationsUnprocessed = numberOfNotificationsUnprocessed;
        this.requestTimestamp = requestTimestamp;
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

    public String getRequestTimestamp() {
        return requestTimestamp;
    }

    @Override
    public DiagnosticModel obfuscate() {
        // Diagnostic model does not handle sensitive data and therefore does not need any fields to be obfuscated
        return new DiagnosticModel(numberOfNotifications, numberOfNotificationsProcessed, numberOfNotificationsUnprocessed, requestTimestamp);
    }
}
