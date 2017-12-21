/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.web.model;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class AuditEntryRestModel extends ConfigRestModel {
    private static final long serialVersionUID = -8714295254107315501L;

    private String name;
    private String eventType;
    private String timeCreated;
    private String timeLastSent;
    private String status;
    private NotificationRestModel notification;
    private String errorMessage;
    private String errorStackTrace;

    public AuditEntryRestModel() {
    }

    public AuditEntryRestModel(final String id, final String name, final String eventType, final String timeCreated, final String timeLastSent, final String status, final String errorMessage, final String errorStackTrace,
            final NotificationRestModel notification) {
        super(id);
        this.name = name;
        this.eventType = eventType;
        this.timeCreated = timeCreated;
        this.timeLastSent = timeLastSent;
        this.status = status;
        this.notification = notification;
        this.errorMessage = errorMessage;
        this.errorStackTrace = errorStackTrace;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getName() {
        return name;
    }

    public String getEventType() {
        return eventType;
    }

    public String getTimeCreated() {
        return timeCreated;
    }

    public String getTimeLastSent() {
        return timeLastSent;
    }

    public String getStatus() {
        return status;
    }

    public NotificationRestModel getNotification() {
        return notification;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorStackTrace() {
        return errorStackTrace;
    }

    @Override
    public String toString() {
        final ReflectionToStringBuilder reflectionToStringBuilder = new ReflectionToStringBuilder(this, RecursiveToStringStyle.JSON_STYLE);
        return reflectionToStringBuilder.build();
    }

}
