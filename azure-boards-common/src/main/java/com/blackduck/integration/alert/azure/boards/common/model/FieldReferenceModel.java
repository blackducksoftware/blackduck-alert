/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.azure.boards.common.model;

public class FieldReferenceModel {
    private String referenceName;
    private String url;

    public FieldReferenceModel() {
        // For serialization
    }

    public FieldReferenceModel(String referenceName, String url) {
        this.referenceName = referenceName;
        this.url = url;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public String getUrl() {
        return url;
    }

}
