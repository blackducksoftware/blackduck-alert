/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.provider;

import java.util.HashMap;
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
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.message.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.provider.EmailHandler;
import com.synopsys.integration.alert.common.rest.model.CommonDistributionConfiguration;

@Component
public class DefaultEmailHandler extends EmailHandler {
    private static final Logger logger = LoggerFactory.getLogger(DefaultEmailHandler.class);

    private final ProviderDataAccessor providerDataAccessor;

    @Autowired
    public DefaultEmailHandler(final ProviderDataAccessor providerDataAccessor) {
        this.providerDataAccessor = providerDataAccessor;
    }

    @Override
    public FieldAccessor updateFieldAccessor(final AggregateMessageContent content, final FieldAccessor originalAccessor) {
        final Set<String> emailAddresses = originalAccessor.getAllStrings(EmailDescriptor.KEY_EMAIL_ADDRESSES).stream().collect(Collectors.toSet());
        final Boolean projectOwnerOnly = originalAccessor.getBoolean(EmailDescriptor.KEY_PROJECT_OWNER_ONLY).orElse(false);
        final Set<String> providerEmailAddresses = collectProviderEmailsFromProject(content.getValue(), projectOwnerOnly);
        emailAddresses.addAll(providerEmailAddresses);

        // Temporary fix for license notifications
        final Set<String> licenseNotificationEmails = licenseNotificationCheck(content, originalAccessor, projectOwnerOnly);
        emailAddresses.addAll(licenseNotificationEmails);

        final Map<String, ConfigurationFieldModel> fieldMap = new HashMap<>();
        fieldMap.putAll(originalAccessor.getFields());
        final ConfigurationFieldModel newEmailFieldModel = ConfigurationFieldModel.create(EmailDescriptor.KEY_EMAIL_ADDRESSES);
        newEmailFieldModel.setFieldValues(emailAddresses);
        fieldMap.put(EmailDescriptor.KEY_EMAIL_ADDRESSES, newEmailFieldModel);
        return new FieldAccessor(fieldMap);
    }

    public Set<String> getEmailAddressesForProject(final ProviderProject project, final Boolean projectOwnerOnly) {
        final Set<String> emailAddresses;
        if (projectOwnerOnly) {
            final String projectOwnerEmail = project.getProjectOwnerEmail();
            if (StringUtils.isNotBlank(projectOwnerEmail)) {
                emailAddresses = Set.of(projectOwnerEmail);
            } else {
                emailAddresses = Set.of();
            }
        } else {
            emailAddresses = providerDataAccessor.getEmailAddressesForProjectHref(project.getHref());
        }
        if (emailAddresses.isEmpty()) {
            logger.error("Could not find any email addresses for project: {}", project.getName());
        }
        return emailAddresses;
    }

    private Set<String> collectProviderEmailsFromProject(final String projectName, final boolean projectOwnerOnly) {
        final Optional<ProviderProject> optionalProject = providerDataAccessor.findFirstByName(projectName); // FIXME use href
        if (optionalProject.isPresent()) {
            return getEmailAddressesForProject(optionalProject.get(), projectOwnerOnly);
        }
        return Set.of();
    }

    // FIXME temporary fix for license notifications before we rewrite the way emails are handled in our workflow
    private Set<String> licenseNotificationCheck(final AggregateMessageContent content, final FieldAccessor fieldAccessor, final boolean projectOwnerOnly) {
        final Optional<LinkableItem> subTopic = content.getSubTopic();
        if (subTopic.isEmpty()) {
            final Boolean filterByProject = fieldAccessor.getBoolean(CommonDistributionConfiguration.KEY_FILTER_BY_PROJECT).orElse(false);
            List<String> associatedProjects = List.of();
            if (filterByProject) {
                associatedProjects = fieldAccessor.getAllStrings(CommonDistributionConfiguration.KEY_CONFIGURED_PROJECT)
                                         .stream()
                                         .collect(Collectors.toList());
            } else {
                final Optional<String> providerName = fieldAccessor.getString(ChannelDistributionUIConfig.KEY_PROVIDER_NAME);
                if (providerName.isPresent()) {
                    associatedProjects = providerDataAccessor.findByProviderName(providerName.get())
                                             .stream()
                                             .map(ProviderProject::getName)
                                             .collect(Collectors.toList());
                }
            }
            return associatedProjects.stream()
                       .map(projectName -> collectProviderEmailsFromProject(projectName, projectOwnerOnly))
                       .flatMap(Set::stream)
                       .collect(Collectors.toSet());
        }

        return Set.of();
    }
}
