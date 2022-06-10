/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.diagnostic.model;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.api.common.model.Obfuscated;

public class DiagnosticModel extends AlertSerializableModel implements Obfuscated<DiagnosticModel> {
    private static final long serialVersionUID = 6714869824373312126L;

    private NotificationDiagnosticModel notificationDiagnosticModel;
    private AuditDiagnosticModel auditDiagnosticModel;
    private String requestTimestamp;

    public DiagnosticModel() {
        // For serialization
    }

    public DiagnosticModel(String requestTimestamp, NotificationDiagnosticModel notificationDiagnosticModel, AuditDiagnosticModel auditDiagnosticModel) {
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
}
