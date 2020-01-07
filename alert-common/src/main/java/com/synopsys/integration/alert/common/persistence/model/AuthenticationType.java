package com.synopsys.integration.alert.common.persistence.model;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class AuthenticationType extends AlertSerializableModel {
    public final static Long AUTH_TYPE_DATABASE = 1L;
    public final static Long AUTH_TYPE_LDAP = 2L;
    public final static Long AUTH_TYPE_SAML = 3L;
    private final Long id;
    private final String name;

    public AuthenticationType(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
