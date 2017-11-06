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

import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserEmailRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserFrequenciesRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserHipChatRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserProjectVersionsRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserSlackRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.ChannelUserRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.UserFrequenciesRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.UserProjectVersionsRepository;
import com.blackducksoftware.integration.hub.alert.digest.DigestTypeEnum;

public class HubUsersWrapper {
    private final Long id;
    private final String username;

    public HubUsersWrapper(final Long id, final String username) {
        this.id = id;
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public DigestTypeEnum getFrequency(final UserFrequenciesRepository userFrequencyRepository) {
        final HubUserFrequenciesRelation frequency = userFrequencyRepository.findOne(getId());
        return DigestTypeEnum.getById(frequency.getFrequencyId());
    }

    // TODO we may not need to read notifications from the database in this way
    // public Collection<Object> getNotifications(final Object userNotificationRepository) {
    // // unimplemented
    // return null;
    // }

    public Collection<HubUserProjectVersionsRelation> getProjectVersions(final UserProjectVersionsRepository userProjectVersionRepository) {
        return userProjectVersionRepository.findByUserId(getId());
    }

    public Long getEmailConfigId(final ChannelUserRepository<HubUserEmailRelation> userEmailRepository) {
        final HubUserEmailRelation emailUserRelation = userEmailRepository.findChannelConfig(getId());
        return emailUserRelation != null ? emailUserRelation.getChannelConfigId() : null;
    }

    public Long getHipChatConfigId(final ChannelUserRepository<HubUserHipChatRelation> userHipChatRepository) {
        final HubUserHipChatRelation hipChatUserRelation = userHipChatRepository.findChannelConfig(getId());
        return hipChatUserRelation != null ? hipChatUserRelation.getChannelConfigId() : null;
    }

    public Long getSlackConfigId(final ChannelUserRepository<HubUserSlackRelation> userSlackRepository) {
        final HubUserSlackRelation slackUserRelation = userSlackRepository.findChannelConfig(getId());
        return slackUserRelation != null ? slackUserRelation.getChannelConfigId() : null;
    }

}
