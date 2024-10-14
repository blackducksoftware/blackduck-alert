/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class ProviderProject extends AlertSerializableModel {
    private static final String[] HASH_CODE_EQUALS_EXCLUDED_FIELDS = { "description" };

    private final String name;
    private final String description;
    private final String href;
    private final String projectOwnerEmail;

    public ProviderProject(String name, String description, String href, String projectOwnerEmail) {
        this.name = name;
        this.description = description;
        this.href = href;
        this.projectOwnerEmail = projectOwnerEmail;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getHref() {
        return href;
    }

    public String getProjectOwnerEmail() {
        return projectOwnerEmail;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, HASH_CODE_EQUALS_EXCLUDED_FIELDS);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, HASH_CODE_EQUALS_EXCLUDED_FIELDS);
    }

}
