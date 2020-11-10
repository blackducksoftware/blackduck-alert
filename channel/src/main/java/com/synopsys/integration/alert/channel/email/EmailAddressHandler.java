/**
 * channel
 *
 * Copyright (c) 2020 Synopsys, Inc.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;

@Component
public class EmailAddressHandler {
    private final Logger logger = LoggerFactory.getLogger(EmailAddressHandler.class);

    private final ProviderDataAccessor providerDataAccessor;

    @Autowired
    public EmailAddressHandler(ProviderDataAccessor providerDataAccessor) {
        this.providerDataAccessor = providerDataAccessor;
    }

    public FieldUtility updateEmailAddresses(Long providerConfigId, MessageContentGroup contentGroup, FieldUtility originalAccessor) {
        Collection<String> allEmailAddresses = originalAccessor.getAllStrings(EmailDescriptor.KEY_EMAIL_ADDRESSES);
        Set<String> emailAddresses = new HashSet<>(allEmailAddresses);

        boolean projectOwnerOnly = originalAccessor.getBoolean(EmailDescriptor.KEY_PROJECT_OWNER_ONLY).orElse(false);
        boolean useOnlyAdditionalEmailAddresses = originalAccessor.getBoolean(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES_ONLY).orElse(false);

        if (!useOnlyAdditionalEmailAddresses) {
            Optional<String> optionalHref = contentGroup.getCommonTopic().getUrl();
            if (optionalHref.isPresent()) {
                Set<String> projectEmailAddresses = collectProviderEmailsFromProject(providerConfigId, optionalHref.get(), projectOwnerOnly);
                emailAddresses.addAll(projectEmailAddresses);
            } else {
                logger.warn("The topic '{}' did not have an href, cannot get emails", contentGroup.getCommonTopic().getName());
            }
        }

        if (emailAddresses.isEmpty()) {
            // Temporary fix for license notifications
            Set<String> licenseNotificationEmails = systemWideNotificationCheck(contentGroup.getSubContent(), originalAccessor, providerConfigId, projectOwnerOnly);
            emailAddresses.addAll(licenseNotificationEmails);
        }

        Set<String> additionalEmailAddresses = collectAdditionalEmailAddresses(providerConfigId, originalAccessor);
        emailAddresses.addAll(additionalEmailAddresses);

        Map<String, ConfigurationFieldModel> fieldMap = new HashMap<>();
        fieldMap.putAll(originalAccessor.getFields());
        ConfigurationFieldModel newEmailFieldModel = ConfigurationFieldModel.create(EmailDescriptor.KEY_EMAIL_ADDRESSES);
        newEmailFieldModel.setFieldValues(emailAddresses);
        fieldMap.put(EmailDescriptor.KEY_EMAIL_ADDRESSES, newEmailFieldModel);
        return new FieldUtility(fieldMap);
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

    public Set<String> collectAdditionalEmailAddresses(Long providerConfigId, FieldUtility fieldUtility) {
        Collection<String> additionalEmailAddresses = fieldUtility.getAllStrings(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES);
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
    private Set<String> systemWideNotificationCheck(Collection<ProviderMessageContent> messages, FieldUtility fieldUtility, Long providerConfigId, boolean projectOwnerOnly) {
        boolean hasSubTopic = messages
                                  .stream()
                                  .map(ProviderMessageContent::getSubTopic)
                                  .anyMatch(Optional::isPresent);
        if (!hasSubTopic) {
            boolean filterByProject = fieldUtility.getBoolean(ProviderDistributionUIConfig.KEY_FILTER_BY_PROJECT).orElse(false);
            List<String> associatedProjects;
            if (filterByProject) {
                Collection<String> allConfiguredProjects = fieldUtility.getAllStrings(ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT);
                associatedProjects = new ArrayList<>(allConfiguredProjects);
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
