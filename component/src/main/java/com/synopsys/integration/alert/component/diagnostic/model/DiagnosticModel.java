package com.synopsys.integration.alert.component.diagnostic.model;

import java.time.LocalDateTime;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.api.common.model.Obfuscated;

public class DiagnosticModel extends AlertSerializableModel implements Obfuscated<DiagnosticModel> {
    private static final long serialVersionUID = 6714869824373312126L;

    private NotificationDiagnosticModel notificationDiagnosticModel;
    private AuditDiagnosticModel auditDiagnosticModel;
    private String requestTimestamp;

    private DiagnosticModel() {
        // For serialization
    }

    private DiagnosticModel(String requestTimestamp, NotificationDiagnosticModel notificationDiagnosticModel, AuditDiagnosticModel auditDiagnosticModel) {
        this.requestTimestamp = requestTimestamp;
        this.notificationDiagnosticModel = notificationDiagnosticModel;
        this.auditDiagnosticModel = auditDiagnosticModel;
    }

    public NotificationDiagnosticModel getNotificationDiagnosticModel() {
        return notificationDiagnosticModel;
    }

    public AuditDiagnosticModel getAuditDiagnosticModel() {
        return auditDiagnosticModel;
    }

    public String getRequestTimestamp() {
        return requestTimestamp;
    }

    @Override
    public DiagnosticModel obfuscate() {
        return new DiagnosticModel(requestTimestamp, notificationDiagnosticModel, auditDiagnosticModel);
    }

    public static class Builder {
        private NotificationDiagnosticModel notificationDiagnosticModel;
        private AuditDiagnosticModel auditDiagnosticModel;

        public DiagnosticModel build() {
            return new DiagnosticModel(
                LocalDateTime.now().toString(),
                notificationDiagnosticModel,
                auditDiagnosticModel
            );
        }

        public Builder notificationDiagnosticModel(NotificationDiagnosticModel notificationDiagnosticModel) {
            this.notificationDiagnosticModel = notificationDiagnosticModel;
            return this;
        }

        public Builder auditDiagnosticModel(AuditDiagnosticModel auditDiagnosticModel) {
            this.auditDiagnosticModel = auditDiagnosticModel;
            return this;
        }
    }
}
