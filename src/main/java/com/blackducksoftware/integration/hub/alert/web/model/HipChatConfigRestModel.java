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
package com.blackducksoftware.integration.hub.alert.web.model;

public class HipChatConfigRestModel extends ChannelRestModel {
    private static final long serialVersionUID = 8852683250883814613L;

    private String apiKey;
    private Integer roomId;
    private Boolean notify;
    private String color;

    protected HipChatConfigRestModel() {
    }

    public HipChatConfigRestModel(final String apiKey, final Integer roomId, final Boolean notify, final String color) {
        this.apiKey = apiKey;
        this.roomId = roomId;
        this.notify = notify;
        this.color = color;
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
}
