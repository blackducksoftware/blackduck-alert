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
package com.blackducksoftware.integration.hub.alert.datasource.entity.model;

import java.util.Collection;

import com.blackducksoftware.integration.hub.alert.datasource.relation.EmailUserRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.HipChatUserRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.ProjectVersionUserRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.SlackUserRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.ChannelUserRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.ProjectVersionUserRepository;

public class UserConfigWrapper {
    private final Long id;
    private final String username;

    public UserConfigWrapper(final Long id, final String username) {
        this.id = id;
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFrequency(final Object userFrequencyRepository) {
        // TODO
        return null;
    }

    public Collection<ProjectVersionUserRelation> getProjectVersions(final ProjectVersionUserRepository projectVersionUserRepository) {
        return projectVersionUserRepository.findByUserId(getId());
    }

    public Long getEmailConfigId(final ChannelUserRepository<EmailUserRelation> emailUserRepository) {
        final EmailUserRelation emailUserRelation = emailUserRepository.findChannelConfig(getId());
        return emailUserRelation != null ? emailUserRelation.getChannelConfigId() : null;
    }

    public Long getHipChatConfigId(final ChannelUserRepository<HipChatUserRelation> hipChatUserRepository) {
        final HipChatUserRelation hipChatUserRelation = hipChatUserRepository.findChannelConfig(getId());
        return hipChatUserRelation != null ? hipChatUserRelation.getChannelConfigId() : null;
    }

    public Long getSlackConfigId(final ChannelUserRepository<SlackUserRelation> slackUserRepository) {
        final SlackUserRelation slackUserRelation = slackUserRepository.findChannelConfig(getId());
        return slackUserRelation != null ? slackUserRelation.getChannelConfigId() : null;
    }

}
