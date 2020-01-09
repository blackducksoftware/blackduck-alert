/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.common.enumeration;

import java.util.Map;

public enum AuthenticationType {
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

    public static AuthenticationType getById(Long id) {
        return ID_TYPE_MAPPING.get(id);
    }

    public Long getId() {
        return id;
    }
}
