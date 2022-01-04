/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.enumeration;

import java.util.Map;
import java.util.Optional;

public enum AuthenticationType {
    UNKNOWN(-1L),
    DATABASE(1L),
    LDAP(2L),
    SAML(3L);

    private static final Map<Long, AuthenticationType> ID_TYPE_MAPPING = Map.of(
        DATABASE.getId(), DATABASE,
        LDAP.getId(), LDAP,
        SAML.getId(), SAML);

    private Long id;

    AuthenticationType(Long databaseId) {
        this.id = databaseId;
    }

    public static Optional<AuthenticationType> getById(Long id) {
        return Optional.ofNullable(ID_TYPE_MAPPING.get(id));
    }

    public Long getId() {
        return id;
    }
}
