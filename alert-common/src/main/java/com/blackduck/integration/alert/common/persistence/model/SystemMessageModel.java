/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.model;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class SystemMessageModel extends AlertSerializableModel {
    private final String id;
    private final String severity;
    private final String createdAt;
    private final String content;
    private final String type;

    public SystemMessageModel(String id, String severity, String createdAt, String content, String type) {
        this.id = id;
        this.severity = severity;
        this.createdAt = createdAt;
        this.content = content;
        this.type = type;
    }

    public String getId() {
        return id;
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
