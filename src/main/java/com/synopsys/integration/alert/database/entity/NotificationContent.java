/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.database.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(schema = "alert", name = "raw_notification_content")
public class NotificationContent extends DatabaseEntity {

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;
    @Column(name = "provider")
    private String provider;
    @Column(name = "notification_type")
    private String notificationType;
    @Column(name = "content")
    private String content;

    public NotificationContent() {
        // JPA requires default constructor definitions
    }

    public NotificationContent(final Date createdAt, final String provider, final String notificationType, final String content) {
        this.createdAt = createdAt;
        this.provider = provider;
        this.notificationType = notificationType;
        this.content = content;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getProvider() {
        return provider;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public String getContent() {
        return content;
    }
}
