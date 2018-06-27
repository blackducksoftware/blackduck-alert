/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.blackducksoftware.integration.hub.alert.datasource.entity.global;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.blackducksoftware.integration.hub.alert.annotation.SensitiveField;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.web.security.StringEncryptionConverter;

@Entity
@Table(schema = "alert", name = "global_hub_config")
public class GlobalHubConfigEntity extends DatabaseEntity {
    @Column(name = "hub_timeout")
    private Integer hubTimeout;

    // @EncryptedStringField
    @Column(name = "hub_api_key")
    @SensitiveField
    @Convert(converter = StringEncryptionConverter.class)
    private String hubApiKey;

    public GlobalHubConfigEntity() {
    }

    public GlobalHubConfigEntity(final Integer hubTimeout, final String hubApiKey) {
        this.hubTimeout = hubTimeout;
        this.hubApiKey = hubApiKey;
    }

    public Integer getHubTimeout() {
        return hubTimeout;
    }

    public String getHubApiKey() {
        return hubApiKey;
    }

}
