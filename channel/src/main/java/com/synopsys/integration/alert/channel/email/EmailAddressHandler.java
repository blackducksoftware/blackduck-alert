/**
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;
import com.synopsys.integration.alert.common.persistence.model.job.BlackDuckProjectDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;

@Component
public class EmailAddressHandler {
    private final Logger logger = LoggerFactory.getLogger(EmailAddressHandler.class);

    private final ProviderDataAccessor providerDataAccessor;

    @Autowired
    public EmailAddressHandler(ProviderDataAccessor providerDataAccessor) {
        this.providerDataAccessor = providerDataAccessor;
    }

    public Set<String> getUpdatedEmailAddresses(Long providerConfigId, MessageContentGroup contentGroup, DistributionJobModel distributionJobModel, EmailJobDetailsModel emailJobDetailsModel) {
        Set<String> emailAddresses = new HashSet<>();

        boolean projectOwnerOnly = emailJobDetailsModel.isProjectOwnerOnly();
        boolean useOnlyAdditionalEmailAddresses = emailJobDetailsModel.isAdditionalEmailAddressesOnly();

        if (!useOnlyAdditionalEmailAddresses) {
            Optional<String> optionalHref = contentGroup.getCommonTopic().getUrl();
            if (optionalHref.isPresent()) {
                Set<String> projectEmailAddresses = collectProviderEmailsFromProject(providerConfigId, optionalHref.get(), projectOwnerOnly);
                emailAddresses.addAll(projectEmailAddresses);
            } else {
                logger.warn("The project '{}' did not have an href, cannot get emails", contentGroup.getCommonTopic().getValue());
            }
        }

        if (emailAddresses.isEmpty()) {
            // Temporary fix for license notifications
            Set<String> licenseNotificationEmails = systemWideNotificationCheck(contentGroup.getSubContent(), distributionJobModel, providerConfigId, projectOwnerOnly);
            emailAddresses.addAll(licenseNotificationEmails);
        }

        Set<String> additionalEmailAddresses = collectAdditionalEmailAddresses(providerConfigId, emailJobDetailsModel);
        emailAddresses.addAll(additionalEmailAddresses);
        return emailAddresses;
    }

    public Set<String> getEmailAddressesForProject(Long providerConfigId, ProviderProject project, boolean projectOwnerOnly) {
        Set<String> emailAddresses;
        if (projectOwnerOnly) {
            String projectOwnerEmail = project.getProjectOwnerEmail();
            if (StringUtils.isNotBlank(projectOwnerEmail)) {
                emailAddresses = Set.of(projectOwnerEmail);
            } else {
                emailAddresses = Set.of();
            }
        } else {
            emailAddresses = providerDataAccessor.getEmailAddressesForProjectHref(providerConfigId, project.getHref());
        }
        if (emailAddresses.isEmpty()) {
            logger.error("Could not find any email addresses for project: {}", project.getName());
        }
        return emailAddresses;
    }

    private Set<String> collectAdditionalEmailAddresses(Long providerConfigId, EmailJobDetailsModel emailJobDetailsModel) {
        Collection<String> additionalEmailAddresses = emailJobDetailsModel.getAdditionalEmailAddresses();
        if (!additionalEmailAddresses.isEmpty()) {
            logger.debug("Adding additional email addresses");
            return providerDataAccessor.getUsersByProviderConfigId(providerConfigId)
                       .stream()
                       .map(ProviderUserModel::getEmailAddress)
                       .filter(additionalEmailAddresses::contains)
                       .collect(Collectors.toSet());
        }
        logger.debug("No additional email addresses to add");
        return Set.of();
    }

    private Set<String> collectProviderEmailsFromProject(Long providerConfigId, String projectHref, boolean projectOwnerOnly) {
        Optional<ProviderProject> optionalProject = providerDataAccessor.getProjectsByProviderConfigId(providerConfigId)
                                                        .stream()
                                                        .filter(project -> project.getHref().equals(projectHref))
                                                        .findFirst();
        if (optionalProject.isPresent()) {
            return getEmailAddressesForProject(providerConfigId, optionalProject.get(), projectOwnerOnly);
        }
        return Set.of();
    }

    // FIXME temporary fix for license notifications before we rewrite the way emails are handled in our workflow
    private Set<String> systemWideNotificationCheck(Collection<ProviderMessageContent> messages, DistributionJobModel distributionJobModel, Long providerConfigId, boolean projectOwnerOnly) {
        boolean hasSubTopic = messages
                                  .stream()
                                  .map(ProviderMessageContent::getSubTopic)
                                  .anyMatch(Optional::isPresent);
        if (!hasSubTopic) {
            boolean filterByProject = distributionJobModel.isFilterByProject();
            List<String> associatedProjects;
            if (filterByProject) {
                associatedProjects = distributionJobModel.getProjectFilterDetails()
                                         .stream()
                                         .map(BlackDuckProjectDetailsModel::getHref)
                                         .collect(Collectors.toList());
            } else {
                associatedProjects = providerDataAccessor.getProjectsByProviderConfigId(providerConfigId)
                                         .stream()
                                         .map(ProviderProject::getHref)
                                         .collect(Collectors.toList());
            }
            return associatedProjects.stream()
                       .map(projectHref -> collectProviderEmailsFromProject(providerConfigId, projectHref, projectOwnerOnly))
                       .flatMap(Set::stream)
                       .collect(Collectors.toSet());
        }
        return Set.of();
    }

}
