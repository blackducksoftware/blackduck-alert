/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.model;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

// TODO rename this to something more descriptive like UserProvidedCertificateModel
public class CustomCertificateModel extends AlertSerializableModel {
    private static final long serialVersionUID = -6655684999195764188L;
    private Long id;
    private final String alias;
    private final String certificateContent;
    private final String lastUpdated;

    public CustomCertificateModel(String alias, String certificateContent, String lastUpdated) {
        this.id = null;
        this.alias = alias;
        this.certificateContent = certificateContent;
        this.lastUpdated = lastUpdated;
    }

    public CustomCertificateModel(Long id, String alias, String certificateContent, String lastUpdated) {
        this.id = id;
        this.alias = alias;
        this.certificateContent = certificateContent;
        this.lastUpdated = lastUpdated;
    }

    public Long getNullableId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAlias() {
        return alias;
    }

    public String getCertificateContent() {
        return certificateContent;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

}
