/**
 * alert-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

    public AuditEntryModel(final String id, final NotificationConfig notification, final List<JobAuditModel> jobs, final String overallStatus, final String lastSent) {
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
