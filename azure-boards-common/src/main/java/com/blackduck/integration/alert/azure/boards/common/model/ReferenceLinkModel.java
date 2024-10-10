/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.azure.boards.common.model;

public class ReferenceLinkModel {
    private String href;

    public ReferenceLinkModel() {
        // For serialization
    }

    public ReferenceLinkModel(String href) {
        this.href = href;
    }

    public String getHref() {
        return href;
    }

}
