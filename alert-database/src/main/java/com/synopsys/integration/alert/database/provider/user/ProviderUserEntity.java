/**
 * alert-database
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
package com.synopsys.integration.alert.database.provider.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.DatabaseEntity;

@Entity
@Table(schema = "alert", name = "provider_users")
public class ProviderUserEntity extends DatabaseEntity {
    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "opt_out")
    private Boolean optOut;

    @Column(name = "provider")
    private String provider;

    public ProviderUserEntity() {
        // JPA requires default constructor definitions
    }

    public ProviderUserEntity(String emailAddress, Boolean optOut, String provider) {
        this.emailAddress = emailAddress;
        this.optOut = optOut;
        this.provider = provider;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public Boolean getOptOut() {
        return optOut;
    }

    public String getProvider() {
        return provider;
    }
}
