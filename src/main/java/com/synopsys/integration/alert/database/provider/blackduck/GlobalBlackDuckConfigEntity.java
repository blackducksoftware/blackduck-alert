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
package com.synopsys.integration.alert.database.provider.blackduck;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.synopsys.integration.alert.common.annotation.SensitiveField;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;

@Entity
@Table(schema = "alert", name = "global_blackduck_config")
public class GlobalBlackDuckConfigEntity extends DatabaseEntity {
    @Column(name = "blackduck_timeout")
    private Integer blackDuckTimeout;

    // @EncryptedStringField
    @Column(name = "blackduck_api_key")
    @SensitiveField
    private String blackDuckApiKey;
    @Column(name = "blackduck_url")
    private String blackDuckUrl;

    public GlobalBlackDuckConfigEntity() {
        // JPA requires default constructor definitions
    }

    public GlobalBlackDuckConfigEntity(final Integer blackDuckTimeout, final String blackDuckApiKey, final String blackDuckUrl) {
        this.blackDuckTimeout = blackDuckTimeout;
        this.blackDuckApiKey = blackDuckApiKey;
        this.blackDuckUrl = blackDuckUrl;
    }

    public Integer getBlackDuckTimeout() {
        return blackDuckTimeout;
    }

    public String getBlackDuckApiKey() {
        return blackDuckApiKey;
    }

    public String getBlackDuckUrl() {
        return blackDuckUrl;
    }
}
