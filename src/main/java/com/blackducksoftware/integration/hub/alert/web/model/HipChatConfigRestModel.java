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

public class HipChatConfigRestModel extends ConfigRestModel {
    private static final long serialVersionUID = 8852683250883814613L;

    private String apiKey;
    private String roomId;
    private String notify;
    private String color;

    public HipChatConfigRestModel() {
    }

    public HipChatConfigRestModel(final String id, final String apiKey, final String roomId, final String notify, final String color) {
        super(id);
        this.apiKey = apiKey;
        this.roomId = roomId;
        this.notify = notify;
        this.color = color;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getNotify() {
        return notify;
    }

    public String getColor() {
        return color;
    }
}
