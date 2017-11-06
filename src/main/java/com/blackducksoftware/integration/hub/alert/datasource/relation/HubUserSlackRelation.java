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
package com.blackducksoftware.integration.hub.alert.datasource.relation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "hub_user_slack", schema = "hub_user")
public class HubUserSlackRelation extends DatabaseRelation {
    private static final long serialVersionUID = -5649425971831549437L;

    @Column(name = "slack_config_id")
    private final Long slackConfigId;

    public HubUserSlackRelation(final Long userConfidId, final Long slackConfigId) {
        super(userConfidId);
        this.slackConfigId = slackConfigId;
    }

    public Long getChannelConfigId() {
        return slackConfigId;
    }
}
