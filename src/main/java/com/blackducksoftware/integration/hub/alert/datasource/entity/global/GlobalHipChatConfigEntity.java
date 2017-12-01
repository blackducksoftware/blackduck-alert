/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;

@Entity
@Table(schema = "alert", name = "global_hipchat_config")
public class GlobalHipChatConfigEntity extends DatabaseEntity {
    private static final long serialVersionUID = 2791949172564090134L;

    @Column(name = "api_key")
    private String apiKey;

    @Column(name = "room_id")
    private Integer roomId;

    @Column(name = "notify")
    private Boolean notify;

    @Column(name = "color")
    private String color;

    public GlobalHipChatConfigEntity() {
    }

    public GlobalHipChatConfigEntity(final String apiKey, final Integer room_id, final Boolean notify, final String color) {
        this.apiKey = apiKey;
        this.roomId = room_id;
        this.notify = notify;
        this.color = color;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getApiKey() {
        return apiKey;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public Boolean getNotify() {
        return notify;
    }

    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        final ReflectionToStringBuilder reflectionToStringBuilder = new ReflectionToStringBuilder(this, RecursiveToStringStyle.JSON_STYLE);
        reflectionToStringBuilder.setExcludeFieldNames("apiKey");
        return reflectionToStringBuilder.build();
    }

}
