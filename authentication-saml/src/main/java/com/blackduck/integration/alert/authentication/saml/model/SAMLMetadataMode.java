/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.authentication.saml.model;

public enum SAMLMetadataMode {
    FILE(0), URL(1);

    private final Integer mode;

    SAMLMetadataMode(Integer mode) {
        this.mode = mode;
    }

    public Integer getMode() {
        return mode;
    }
}
