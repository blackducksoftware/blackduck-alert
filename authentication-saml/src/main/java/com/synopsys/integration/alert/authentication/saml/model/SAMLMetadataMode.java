package com.synopsys.integration.alert.authentication.saml.model;

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
