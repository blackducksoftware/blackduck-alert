/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.provider.blackduck;

import java.util.HashMap;
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
import com.synopsys.integration.alert.common.message.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.provider.EmailHandler;
import com.synopsys.integration.alert.database.api.ProviderDataAccessor;

@Component
public class BlackDuckEmailHandler extends EmailHandler {
    private static final Logger logger = LoggerFactory.getLogger(BlackDuckEmailHandler.class);

    private final ProviderDataAccessor providerDataAccessor;

    @Autowired
    public BlackDuckEmailHandler(final ProviderDataAccessor providerDataAccessor) {
        this.providerDataAccessor = providerDataAccessor;
    }

    @Override
    public FieldAccessor updateFieldAccessor(final AggregateMessageContent content, final FieldAccessor originalAccessor) {
        final Set<String> emailAddresses = originalAccessor.getAllStrings(EmailDescriptor.KEY_EMAIL_ADDRESSES).stream().collect(Collectors.toSet());
        final Boolean projectOwnerOnly = originalAccessor.getBoolean(EmailDescriptor.KEY_PROJECT_OWNER_ONLY).orElse(false);
        final Set<String> blackDuckEmailAddresses = collectBlackDuckEmailsFromProject(content.getValue(), projectOwnerOnly);
        emailAddresses.addAll(blackDuckEmailAddresses);

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

    private Set<String> collectBlackDuckEmailsFromProject(final String projectName, final boolean projectOwnerOnly) {
        final Optional<ProviderProject> optionalProject = providerDataAccessor.findByName(projectName); // FIXME use href
        if (optionalProject.isPresent()) {
            return getEmailAddressesForProject(optionalProject.get(), projectOwnerOnly);
        }
        return Set.of();
    }
}
