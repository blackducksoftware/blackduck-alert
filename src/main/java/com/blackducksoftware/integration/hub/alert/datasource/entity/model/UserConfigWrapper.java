/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
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
