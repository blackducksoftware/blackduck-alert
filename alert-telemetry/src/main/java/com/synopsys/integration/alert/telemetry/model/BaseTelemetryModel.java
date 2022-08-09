/*
 * alert-telemetry
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.telemetry.model;

import java.time.OffsetDateTime;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public abstract class BaseTelemetryModel extends AlertSerializableModel {
    private OffsetDateTime startTaskTime;
    private OffsetDateTime completeTaskTime;

    protected BaseTelemetryModel() {
    }

    protected BaseTelemetryModel(OffsetDateTime startTaskTime, @Nullable OffsetDateTime completeTaskTime) {
        this.startTaskTime = startTaskTime;
        this.completeTaskTime = completeTaskTime;
    }

    public OffsetDateTime getStartTaskTime() {
        return startTaskTime;
    }

    public void setStartTaskTime(OffsetDateTime startTaskTime) {
        this.startTaskTime = startTaskTime;
    }

    public OffsetDateTime getCompleteTaskTime() {
        return completeTaskTime;
    }

    public void setCompleteTaskTime(OffsetDateTime completeTaskTime) {
        this.completeTaskTime = completeTaskTime;
    }
}
