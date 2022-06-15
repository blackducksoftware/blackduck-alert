/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.diagnostic.model;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class SystemDiagnosticModel extends AlertSerializableModel {
    private static final long serialVersionUID = 1442481609300011906L;
    private final int availableProcessors;
    private final long maxMemory;
    private final long totalMemory;
    private final long freeMemory;
    private final long usedMemory;

    public SystemDiagnosticModel(int availableProcessors, long maxMemory, long totalMemory, long freeMemory) {
        this.availableProcessors = availableProcessors;
        this.maxMemory = maxMemory;
        this.totalMemory = totalMemory;
        this.freeMemory = freeMemory;
        this.usedMemory = totalMemory - freeMemory;
    }

    public int getAvailableProcessors() {
        return availableProcessors;
    }

    public long getMaxMemory() {
        return maxMemory;
    }

    public long getTotalMemory() {
        return totalMemory;
    }

    public long getFreeMemory() {
        return freeMemory;
    }

    public long getUsedMemory() {
        return usedMemory;
    }
}
