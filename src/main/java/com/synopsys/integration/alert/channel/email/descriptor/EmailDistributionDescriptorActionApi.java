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
package com.synopsys.integration.alert.channel.email.descriptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.EmailGroupChannel;
import com.synopsys.integration.alert.common.descriptor.config.ChannelDistributionDescriptorActionApi;
import com.synopsys.integration.alert.database.channel.email.EmailDistributionRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.web.channel.model.EmailDistributionConfig;
import com.synopsys.integration.alert.web.exception.AlertFieldException;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.alert.web.model.TestConfigModel;

@Component
public class EmailDistributionDescriptorActionApi extends ChannelDistributionDescriptorActionApi {
    private final EmailGroupChannel emailGroupChannel;
    private final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor;

    @Autowired
    public EmailDistributionDescriptorActionApi(final EmailDistributionTypeConverter databaseContentConverter, final EmailDistributionRepositoryAccessor repositoryAccessor, final EmailGroupChannel emailGroupChannel,
        final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor) {
        super(databaseContentConverter, repositoryAccessor, emailGroupChannel);
        this.emailGroupChannel = emailGroupChannel;
        this.blackDuckProjectRepositoryAccessor = blackDuckProjectRepositoryAccessor;
    }

    @Override
    public void validateConfig(final Config restModel, final Map<String, String> fieldErrors) {
    }

    @Override
    public TestConfigModel createTestConfigModel(final Config config, final String destination) throws AlertFieldException {
        final EmailDistributionConfig emailDistributionConfig = (EmailDistributionConfig) config;

        final Set<String> emailAddresses = new HashSet<>();
        Set<BlackDuckProjectEntity> blackDuckProjectEntities = null;
        if (BooleanUtils.toBoolean(emailDistributionConfig.getFilterByProject())) {
            blackDuckProjectEntities = blackDuckProjectRepositoryAccessor.readEntities()
                                           .stream()
                                           .map(databaseEntity -> (BlackDuckProjectEntity) databaseEntity)
                                           .filter(databaseEntity -> emailGroupChannel.doesProjectNameMatchThePattern(databaseEntity.getName(), emailDistributionConfig.getProjectNamePattern())
                                                                         || emailGroupChannel.doesProjectNameMatchAConfiguredProject(databaseEntity.getName(), emailDistributionConfig.getConfiguredProjects()))
                                           .collect(Collectors.toSet());
        } else if (BlackDuckProvider.COMPONENT_NAME.equals(emailDistributionConfig.getProviderName())) {
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
                    final Set<String> emailsForProject = emailGroupChannel.getEmailAddressesForProject(project, emailDistributionConfig.getProjectOwnerOnly());
                    if (emailsForProject.isEmpty()) {
                        projectsWithoutEmails.add(project.getName());
                    }
                    emailAddresses.addAll(emailsForProject);
                });
            if (!projectsWithoutEmails.isEmpty()) {
                final String projects = StringUtils.join(projectsWithoutEmails, ", ");
                final Map<String, String> fieldErrors = new HashMap<>();
                final String errorMessage;
                if (emailDistributionConfig.getProjectOwnerOnly()) {
                    errorMessage = String.format("Could not find Project owners for the projects: %s", projects);
                } else {
                    errorMessage = String.format("Could not find any email addresses for the projects: %s", projects);
                }
                fieldErrors.put("configuredProjects", errorMessage);
                throw new AlertFieldException(fieldErrors);
            }
        }

        emailDistributionConfig.setEmailAddresses(emailAddresses);
        return super.createTestConfigModel(emailDistributionConfig, destination);
    }
}
