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
package com.synopsys.integration.alert.channel.email;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelation;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelationRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;

@Component
public class EmailAddressHandler {
    private static final Logger logger = LoggerFactory.getLogger(EmailAddressHandler.class);

    private final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor;
    private final UserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor;
    private final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor;

    @Autowired
    public EmailAddressHandler(final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor,
        final UserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor, final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor) {
        this.blackDuckProjectRepositoryAccessor = blackDuckProjectRepositoryAccessor;
        this.userProjectRelationRepositoryAccessor = userProjectRelationRepositoryAccessor;
        this.blackDuckUserRepositoryAccessor = blackDuckUserRepositoryAccessor;
    }

    public FieldAccessor updateEmailAddresses(final String provider, final AggregateMessageContent content, final FieldAccessor originalAccessor) {
        if (StringUtils.isBlank(provider)) {
            return originalAccessor;
        }
        if (BlackDuckProvider.COMPONENT_NAME.equals(provider)) {
            Set<String> emailAddresses = originalAccessor.getAllStrings(EmailDescriptor.KEY_EMAIL_ADDRESSES).stream().collect(Collectors.toSet());
            final Boolean projectOwnerOnly = originalAccessor.getBoolean(EmailDescriptor.KEY_PROJECT_OWNER_ONLY).orElse(false);
            emailAddresses = populateBlackDuckEmails(emailAddresses, content.getValue(), projectOwnerOnly);

            final Map<String, ConfigurationFieldModel> fieldMap = new HashMap<>();
            fieldMap.putAll(originalAccessor.getFields());
            final ConfigurationFieldModel newEmailFieldModel = ConfigurationFieldModel.create(EmailDescriptor.KEY_EMAIL_ADDRESSES);
            newEmailFieldModel.setFieldValues(emailAddresses);
            fieldMap.put(EmailDescriptor.KEY_EMAIL_ADDRESSES, newEmailFieldModel);
            return new FieldAccessor(fieldMap);
        }
        return originalAccessor;
    }

    public Set<String> getBlackDuckEmailAddressesForProject(final BlackDuckProjectEntity blackDuckProjectEntity, final boolean projectOwnerOnly) {
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

    private Set<String> populateBlackDuckEmails(Set<String> emailAddresses, final String projectName, final boolean projectOwnerOnly) {
        if (null != emailAddresses && !emailAddresses.isEmpty()) {
            return emailAddresses;
        }
        final BlackDuckProjectEntity projectEntity = blackDuckProjectRepositoryAccessor.findByName(projectName);
        emailAddresses = getBlackDuckEmailAddressesForProject(projectEntity, projectOwnerOnly);
        if (emailAddresses.isEmpty()) {
            logger.error("Could not find any email addresses for project: {}", projectName);
        }
        return emailAddresses;
    }
}
