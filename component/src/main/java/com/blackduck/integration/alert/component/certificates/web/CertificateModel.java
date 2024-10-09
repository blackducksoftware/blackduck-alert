/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.certificates.web;

import com.blackduck.integration.alert.common.rest.model.Config;

public class CertificateModel extends Config {
    private static final long serialVersionUID = 5148208006398190462L;
    private String alias;
    private String certificateContent;
    private String lastUpdated;

    public CertificateModel() {
        super();
    }

    public CertificateModel(String alias, String certificateContent, String lastUpdated) {
        this.alias = alias;
        this.certificateContent = certificateContent;
        this.lastUpdated = lastUpdated;
    }

    public CertificateModel(String id, String alias, String certificateContent, String lastUpdated) {
        super(id);
        this.alias = alias;
        this.certificateContent = certificateContent;
        this.lastUpdated = lastUpdated;
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
