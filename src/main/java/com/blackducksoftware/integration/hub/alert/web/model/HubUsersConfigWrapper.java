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

import java.util.List;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class HubUsersConfigWrapper extends ConfigRestModel {
    private static final long serialVersionUID = 7272140956458105198L;

    private String username;
    private String frequency;
    private String emailConfigId;
    private String hipChatConfigId;
    private String slackConfigId;
    private String active;
    private List<ProjectVersionConfigWrapper> projectVersions;

    public HubUsersConfigWrapper() {
    }

    public HubUsersConfigWrapper(final String id, final String username, final String frequency, final String emailConfigId, final String hipChatConfigId, final String slackConfigId, final String active,
            final List<ProjectVersionConfigWrapper> projectVersions) {
        super(id);
        this.username = username;
        this.frequency = frequency;
        this.emailConfigId = emailConfigId;
        this.hipChatConfigId = hipChatConfigId;
        this.slackConfigId = slackConfigId;
        this.active = active;
        this.projectVersions = projectVersions;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getUsername() {
        return username;
    }

    public String getFrequency() {
        return frequency;
    }

    public String getEmailConfigId() {
        return emailConfigId;
    }

    public String getHipChatConfigId() {
        return hipChatConfigId;
    }

    public String getSlackConfigId() {
        return slackConfigId;
    }

    public String getActive() {
        return active;
    }

    public void setActive(final String active) {
        this.active = active;
    }

    public List<ProjectVersionConfigWrapper> getProjectVersions() {
        return projectVersions;
    }

    @Override
    public String toString() {
        final ReflectionToStringBuilder reflectionToStringBuilder = new ReflectionToStringBuilder(this, RecursiveToStringStyle.JSON_STYLE);
        return reflectionToStringBuilder.toString();
    }

}
