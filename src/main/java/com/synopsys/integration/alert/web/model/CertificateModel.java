package com.synopsys.integration.alert.web.model;

import com.synopsys.integration.alert.common.rest.model.Config;

public class CertificateModel extends Config {
    private static final long serialVersionUID = 5148208006398190462L;
    private String alias;
    private String certificateContent;

    public CertificateModel(String alias, String certificateContent) {
        this.alias = alias;
        this.certificateContent = certificateContent;
    }

    public CertificateModel(String id, String alias, String certificateContent) {
        super(id);
        this.alias = alias;
        this.certificateContent = certificateContent;
    }

    public String getAlias() {
        return alias;
    }

    public String getCertificateContent() {
        return certificateContent;
    }
}
