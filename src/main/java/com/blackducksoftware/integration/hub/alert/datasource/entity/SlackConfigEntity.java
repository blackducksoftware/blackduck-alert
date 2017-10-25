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
package com.blackducksoftware.integration.hub.alert.datasource.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "slack_config", schema = "configuration")
public class SlackConfigEntity extends DatabaseEntity {
    private static final long serialVersionUID = 4872590789715928839L;

    @Column(name = "slack_channel_name")
    private String slackChannelName;

    @Column(name = "username")
    private String username;

    protected SlackConfigEntity() {
    }

    public SlackConfigEntity(final String slackChannelName, final String username) {
        super();
        this.slackChannelName = slackChannelName;
        this.username = username;
    }

    public String getSlackChannelName() {
        return slackChannelName;
    }

    public void setSlackChannelName(final String slackChannelName) {
        this.slackChannelName = slackChannelName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }
}
