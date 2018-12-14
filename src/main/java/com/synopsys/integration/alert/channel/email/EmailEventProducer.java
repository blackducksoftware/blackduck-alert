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
package com.synopsys.integration.alert.channel.email;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.event.ChannelEventProducer;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelation;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelationRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.web.channel.model.EmailDistributionConfig;
import com.synopsys.integration.alert.web.exception.AlertFieldException;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;
import com.synopsys.integration.rest.RestConstants;

@Component
public class EmailEventProducer extends ChannelEventProducer {
    private final Logger logger = LoggerFactory.getLogger(EmailEventProducer.class);

    private final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor;
    private final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor;
    private final UserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor;

    @Autowired
    public EmailEventProducer(final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor,
        final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor, final UserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor) {
        this.blackDuckProjectRepositoryAccessor = blackDuckProjectRepositoryAccessor;
        this.blackDuckUserRepositoryAccessor = blackDuckUserRepositoryAccessor;
        this.userProjectRelationRepositoryAccessor = userProjectRelationRepositoryAccessor;
    }

    @Override
    public EmailChannelEvent createChannelEvent(final CommonDistributionConfig commonDistributionConfig, final AggregateMessageContent messageContent) {
        final EmailDistributionConfig emailDistributionConfig = (EmailDistributionConfig) commonDistributionConfig;
        final String projectName = messageContent.getValue();
        final BlackDuckProjectEntity projectEntity = blackDuckProjectRepositoryAccessor.findByName(projectName);
        final Set<String> emailAddresses = getEmailAddressesForProject(projectEntity, emailDistributionConfig.getProjectOwnerOnly());
        if (emailAddresses.isEmpty()) {
            logger.error("Could not find any email addresses for project: {}. Job: {}", projectName, emailDistributionConfig.getName());
        }
        return new EmailChannelEvent(RestConstants.formatDate(new Date()), emailDistributionConfig.getProviderName(), emailDistributionConfig.getFormatType(), messageContent,
            Long.valueOf(emailDistributionConfig.getId()), getEmailAddressesForProject(projectEntity, emailDistributionConfig.getProjectOwnerOnly()), emailDistributionConfig.getEmailSubjectLine());

    }

    @Override
    public EmailChannelEvent createChannelTestEvent(final CommonDistributionConfig commonDistributionConfig) throws AlertFieldException {
        final AggregateMessageContent messageContent = createTestNotificationContent();

        final EmailDistributionConfig emailDistributionConfig = (EmailDistributionConfig) commonDistributionConfig;

        final Set<String> emailAddresses = new HashSet<>();
        Set<BlackDuckProjectEntity> blackDuckProjectEntities = null;
        if (BooleanUtils.toBoolean(emailDistributionConfig.getFilterByProject())) {
            blackDuckProjectEntities = blackDuckProjectRepositoryAccessor.readEntities()
                                           .stream()
                                           .map(databaseEntity -> (BlackDuckProjectEntity) databaseEntity)
                                           .filter(databaseEntity -> doesProjectNameMatchThePattern(databaseEntity.getName(), emailDistributionConfig.getProjectNamePattern())
                                                                         || doesProjectNameMatchAConfiguredProject(databaseEntity.getName(), emailDistributionConfig.getConfiguredProjects()))
                                           .collect(Collectors.toSet());
        } else if (emailDistributionConfig.getProviderName().equals(BlackDuckProvider.COMPONENT_NAME)) {
            blackDuckProjectEntities = blackDuckProjectRepositoryAccessor.readEntities()
                                           .stream()
                                           .map(databaseEntity -> (BlackDuckProjectEntity) databaseEntity)
                                           .collect(Collectors.toSet());

        }
        if (null != blackDuckProjectEntities) {
            final Set<String> projectsWithoutEmails = new HashSet<>();
            blackDuckProjectEntities
                .stream()
                .forEach(project -> {
                    final Set<String> emailsForProject = getEmailAddressesForProject(project, emailDistributionConfig.getProjectOwnerOnly());
                    if (emailsForProject.isEmpty()) {
                        projectsWithoutEmails.add(project.getName());
                    }
                    emailAddresses.addAll(emailsForProject);
                });
            if (!projectsWithoutEmails.isEmpty()) {
                final String projects = StringUtils.join(projectsWithoutEmails, ", ");
                final Map<String, String> fieldErrors = new HashMap<>();
                String errorMessage = "";
                if (emailDistributionConfig.getProjectOwnerOnly()) {
                    errorMessage = String.format("Could not find Project owners for the projects: %s", projects);
                } else {
                    errorMessage = String.format("Could not find any email addresses for the projects: %s", projects);
                }
                fieldErrors.put("configuredProjects", errorMessage);
                throw new AlertFieldException(fieldErrors);
            }
        }
        return new EmailChannelEvent(RestConstants.formatDate(new Date()), emailDistributionConfig.getProviderName(), emailDistributionConfig.getFormatType(), messageContent,
            null, emailAddresses, emailDistributionConfig.getEmailSubjectLine());
    }

    private Set<String> getEmailAddressesForProject(final BlackDuckProjectEntity blackDuckProjectEntity, final boolean projectOwnerOnly) {
        if (null == blackDuckProjectEntity) {
            return Collections.emptySet();
        }
        final Set<String> emailAddresses;
        if (projectOwnerOnly) {
            emailAddresses = new HashSet<>();
            if (StringUtils.isNotBlank(blackDuckProjectEntity.getProjectOwnerEmail())) {
                emailAddresses.add(blackDuckProjectEntity.getProjectOwnerEmail());
            }
        } else {
            final List<UserProjectRelation> userProjectRelations = userProjectRelationRepositoryAccessor.findByBlackDuckProjectId(blackDuckProjectEntity.getId());
            emailAddresses = userProjectRelations
                                 .stream()
                                 .map(userProjectRelation -> blackDuckUserRepositoryAccessor.readEntity(userProjectRelation.getBlackDuckUserId()))
                                 .filter(userEntity -> userEntity.isPresent())
                                 .map(databaseEntity -> (BlackDuckUserEntity) databaseEntity.get())
                                 .filter(userEntity -> StringUtils.isNotBlank(userEntity.getEmailAddress()))
                                 .map(userEntity -> userEntity.getEmailAddress())
                                 .collect(Collectors.toSet());
        }
        return emailAddresses;
    }

    private boolean doesProjectNameMatchThePattern(final String currentProjectName, final String projectNamePattern) {
        if (StringUtils.isNotBlank(currentProjectName) && StringUtils.isNotBlank(projectNamePattern)) {
            return currentProjectName.matches(projectNamePattern);
        }
        return false;
    }

    private boolean doesProjectNameMatchAConfiguredProject(final String currentProjectName, final List<String> configuredProjectNames) {
        if (StringUtils.isNotBlank(currentProjectName) && null != configuredProjectNames && !configuredProjectNames.isEmpty()) {
            return configuredProjectNames.contains(currentProjectName);
        }
        return false;
    }
}
