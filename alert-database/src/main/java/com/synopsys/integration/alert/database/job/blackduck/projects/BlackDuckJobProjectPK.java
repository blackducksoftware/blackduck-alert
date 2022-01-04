/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.job.blackduck.projects;

import java.io.Serializable;
import java.util.UUID;

public class BlackDuckJobProjectPK implements Serializable {
    private UUID jobId;
    private String href;

    public BlackDuckJobProjectPK() {
    }

    public BlackDuckJobProjectPK(UUID jobId, String href) {
        this.jobId = jobId;
        this.href = href;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

}
