/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.rest.model;

import com.synopsys.integration.alert.common.persistence.model.AuditJobStatusModel;

public class JobAuditModel extends Config {
    private String configId;
    private String name;
    private String eventType;
    private AuditJobStatusModel auditJobStatusModel;
    private String errorMessage;
    private String errorStackTrace;

    public JobAuditModel() {
    }

    public JobAuditModel(final String id, final String configId, final String name, final String eventType, final AuditJobStatusModel auditJobStatusModel, final String errorMessage, final String errorStackTrace) {
        super(id);
        this.configId = configId;
        this.name = name;
        this.eventType = eventType;
        this.auditJobStatusModel = auditJobStatusModel;
        this.errorMessage = errorMessage;
        this.errorStackTrace = errorStackTrace;
    }

    public String getConfigId() {
        return configId;
    }

    public String getName() {
        return name;
    }

    public String getEventType() {
        return eventType;
    }

    public AuditJobStatusModel getAuditJobStatusModel() {
        return auditJobStatusModel;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorStackTrace() {
        return errorStackTrace;
    }

}
