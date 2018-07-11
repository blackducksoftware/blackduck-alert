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
package com.blackducksoftware.integration.alert.channel.hipchat.model;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.blackducksoftware.integration.alert.annotation.SensitiveField;
import com.blackducksoftware.integration.alert.datasource.entity.channel.GlobalChannelConfigEntity;
import com.blackducksoftware.integration.alert.web.security.StringEncryptionConverter;

@Entity
@Table(schema = "alert", name = "global_hipchat_config")
public class GlobalHipChatConfigEntity extends GlobalChannelConfigEntity {

    // @EncryptedStringField
    @Column(name = "api_key")
    @SensitiveField
    @Convert(converter = StringEncryptionConverter.class)
    private String apiKey;

    @Column(name = "host_server")
    private String hostServer;

    public GlobalHipChatConfigEntity() {
    }

    public GlobalHipChatConfigEntity(final String apiKey, final String hostServer) {
        this.apiKey = apiKey;
        this.hostServer = hostServer;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getHostServer() {
        return hostServer;
    }

}
