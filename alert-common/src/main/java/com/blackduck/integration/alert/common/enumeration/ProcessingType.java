/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.enumeration;

public enum ProcessingType {
    DEFAULT("Default", "The message will contain all the relevant data found in your selected provider."),
    DIGEST("Digest", "The message will contain a delta of the content found in your selected provider since it was last queried."
                         + " Add and Delete operations will cancel each other out depending on the order they occurred."),
    SUMMARY("Summary", "The message contains only a summarized form of the Digest data.");

    private String label;
    private String description;

    ProcessingType(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

}
