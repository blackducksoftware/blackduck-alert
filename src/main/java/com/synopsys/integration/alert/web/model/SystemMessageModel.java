/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.web.model;

public class SystemMessageModel {
    private final String severity;
    private final String createdAt;
    private final String content;
    private final String type;

    public SystemMessageModel(final String severity, final String createdAt, final String content, final String type) {
        this.severity = severity;
        this.createdAt = createdAt;
        this.content = content;
        this.type = type;
    }

    public String getSeverity() {
        return severity;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getContent() {
        return content;
    }

    public String getType() {
        return type;
    }
}
