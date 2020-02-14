/**
 * blackduck-alert
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
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;

@Component
public class EmailAddressHandler {
    private static final Logger logger = LoggerFactory.getLogger(EmailAddressHandler.class);

    private final ProviderDataAccessor providerDataAccessor;

    @Autowired
    public EmailAddressHandler(ProviderDataAccessor providerDataAccessor) {
        this.providerDataAccessor = providerDataAccessor;
    }

    // FIXME This does not filter by provider. This works fine for now but will cause bugs in the future when we have multiple providers. Will probably need to modify our tables
    public FieldAccessor updateEmailAddresses(MessageContentGroup contentGroup, FieldAccessor originalAccessor) {
        Collection<String> allEmailAddresses = originalAccessor.getAllStrings(EmailDescriptor.KEY_EMAIL_ADDRESSES);
        Set<String> emailAddresses = new HashSet<>(allEmailAddresses);
        Boolean projectOwnerOnly = originalAccessor.getBoolean(EmailDescriptor.KEY_PROJECT_OWNER_ONLY).orElse(false);

        boolean useOnlyAdditionalEmailAddresses = originalAccessor.getBoolean(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES_ONLY).orElse(false);
        if (!useOnlyAdditionalEmailAddresses) {
            Set<String> projectEmailAddresses = collectProviderEmailsFromProject(contentGroup.getCommonTopic().getValue(), projectOwnerOnly);
            emailAddresses.addAll(projectEmailAddresses);
        }

        if (emailAddresses.isEmpty()) {
            // Temporary fix for license notifications
            Set<String> licenseNotificationEmails = systemWideNotificationCheck(contentGroup.getSubContent(), originalAccessor, projectOwnerOnly);
            emailAddresses.addAll(licenseNotificationEmails);
        }

        Set<String> additionalEmailAddresses = collectAdditionalEmailAddresses(originalAccessor);
        emailAddresses.addAll(additionalEmailAddresses);

        Map<String, ConfigurationFieldModel> fieldMap = new HashMap<>();
        fieldMap.putAll(originalAccessor.getFields());
        ConfigurationFieldModel newEmailFieldModel = ConfigurationFieldModel.create(EmailDescriptor.KEY_EMAIL_ADDRESSES);
        newEmailFieldModel.setFieldValues(emailAddresses);
        fieldMap.put(EmailDescriptor.KEY_EMAIL_ADDRESSES, newEmailFieldModel);
        return new FieldAccessor(fieldMap);
    }

    public Set<String> getEmailAddressesForProject(ProviderProject project, Boolean projectOwnerOnly) {
        Set<String> emailAddresses;
        if (projectOwnerOnly) {
            String projectOwnerEmail = project.getProjectOwnerEmail();
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

    public Set<String> collectAdditionalEmailAddresses(FieldAccessor fieldAccessor) {
        Optional<String> optionalProviderName = fieldAccessor.getString(ChannelDistributionUIConfig.KEY_PROVIDER_NAME);
        Collection<String> additionalEmailAddresses = fieldAccessor.getAllStrings(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES);
        if (optionalProviderName.isPresent() && !additionalEmailAddresses.isEmpty()) {
            logger.debug("Adding additional email addresses");
            // FIXME replace this with provider config lookup
            //            return providerDataAccessor.getAllUsers(optionalProviderName.get())
            //                       .stream()
            //                       .map(ProviderUserModel::getEmailAddress)
            //                       .filter(additionalEmailAddresses::contains)
            //                       .collect(Collectors.toSet());
        }
        logger.debug("No additional email addresses to add");
        return Set.of();
    }

    private Set<String> collectProviderEmailsFromProject(String projectName, boolean projectOwnerOnly) {
        Optional<ProviderProject> optionalProject = Optional.empty(); // FIXME replace with real lookup providerDataAccessor.findFirstByName(projectName); // FIXME use href
        if (optionalProject.isPresent()) {
            return getEmailAddressesForProject(optionalProject.get(), projectOwnerOnly);
        }
        return Set.of();
    }

    // FIXME temporary fix for license notifications before we rewrite the way emails are handled in our workflow
    private Set<String> systemWideNotificationCheck(Collection<ProviderMessageContent> messages, FieldAccessor fieldAccessor, boolean projectOwnerOnly) {
        boolean hasSubTopic = messages
                                  .stream()
                                  .map(ProviderMessageContent::getSubTopic)
                                  .anyMatch(Optional::isPresent);
        if (!hasSubTopic) {
            Boolean filterByProject = fieldAccessor.getBoolean(ProviderDistributionUIConfig.KEY_FILTER_BY_PROJECT).orElse(false);
            List<String> associatedProjects = List.of();
            if (filterByProject) {
                Collection<String> allConfiguredProjects = fieldAccessor.getAllStrings(ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT);
                associatedProjects = new ArrayList<>(allConfiguredProjects);
            } else {
                // FIXME replace this with provider config
                //                Optional<String> providerName = fieldAccessor.getString(ChannelDistributionUIConfig.KEY_PROVIDER_NAME);
                //                if (providerName.isPresent()) {
                //                    associatedProjects = providerDataAccessor.findByProviderName(providerName.get())
                //                                             .stream()
                //                                             .map(ProviderProject::getName)
                //                                             .collect(Collectors.toList());
                //                }
            }
            return associatedProjects.stream()
                       .map(projectName -> collectProviderEmailsFromProject(projectName, projectOwnerOnly))
                       .flatMap(Set::stream)
                       .collect(Collectors.toSet());
        }
        return Set.of();
    }

}
