/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model;

import java.util.List;

import com.synopsys.integration.alert.common.rest.model.Config;
import com.synopsys.integration.alert.common.rest.model.JobAuditModel;
import com.synopsys.integration.alert.common.rest.model.NotificationConfig;

public class AuditEntryModel extends Config {
    private NotificationConfig notification;
    private List<JobAuditModel> jobs;
    private String overallStatus;
    private String lastSent;

    public AuditEntryModel() {
    }

    public AuditEntryModel(String id, NotificationConfig notification, List<JobAuditModel> jobs, String overallStatus, String lastSent) {
        super(id);
        this.notification = notification;
        this.jobs = jobs;
        this.overallStatus = overallStatus;
        this.lastSent = lastSent;
    }

    public NotificationConfig getNotification() {
        return notification;
    }

    public List<JobAuditModel> getJobs() {
        return jobs;
    }

    public String getOverallStatus() {
        return overallStatus;
    }

    public String getLastSent() {
        return lastSent;
    }

}
