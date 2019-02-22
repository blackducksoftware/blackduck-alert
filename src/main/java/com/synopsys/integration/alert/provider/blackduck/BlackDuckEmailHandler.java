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

import java.util.Collections;
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
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.message.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.provider.EmailHandler;
import com.synopsys.integration.alert.database.api.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.database.api.BlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.BlackDuckProjectEntity;
import com.synopsys.integration.alert.database.provider.blackduck.BlackDuckUserEntity;
import com.synopsys.integration.alert.database.provider.blackduck.UserProjectRelation;
import com.synopsys.integration.alert.database.provider.blackduck.UserProjectRelationRepositoryAccessor;

@Component
public class BlackDuckEmailHandler extends EmailHandler {
    private static final Logger logger = LoggerFactory.getLogger(BlackDuckEmailHandler.class);

    private final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor;
    private final UserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor;
    private final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor;

    @Autowired
    public BlackDuckEmailHandler(final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor,
        final UserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor, final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor) {
        this.blackDuckProjectRepositoryAccessor = blackDuckProjectRepositoryAccessor;
        this.userProjectRelationRepositoryAccessor = userProjectRelationRepositoryAccessor;
        this.blackDuckUserRepositoryAccessor = blackDuckUserRepositoryAccessor;
    }

    @Override
    public FieldAccessor updateFieldAccessor(final AggregateMessageContent content, final FieldAccessor originalAccessor) {
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
                                 .flatMap(Optional::stream)
                                 .map(BlackDuckUserEntity::getEmailAddress)
                                 .filter(StringUtils::isNotBlank)
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
