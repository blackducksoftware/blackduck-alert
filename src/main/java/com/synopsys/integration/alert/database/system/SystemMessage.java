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
package com.synopsys.integration.alert.database.system;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;

@Entity
@Table(schema = "alert", name = "system_messages")
public class SystemMessage extends DatabaseEntity {
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date created;
    @Column(name = "severity")
    private String severity;
    @Column(name = "content")
    private String content;

    public SystemMessage() {
    }

    public SystemMessage(final Date created, final String severity, final String content) {
        this.created = created;
        this.severity = severity;
        this.content = content;
    }

    public Date getCreated() {
        return created;
    }

    public String getSeverity() {
        return severity;
    }

    public String getContent() {
        return content;
    }
}
