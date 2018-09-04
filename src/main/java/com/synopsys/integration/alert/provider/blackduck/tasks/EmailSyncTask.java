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
package com.synopsys.integration.alert.provider.blackduck.tasks;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.workflow.scheduled.ScheduledTask;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.rest.BlackduckRestConnection;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.blackduck.service.HubServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;

@Component
public class EmailSyncTask extends ScheduledTask {
    private final Logger logger = LoggerFactory.getLogger(EmailSyncTask.class);
    private final BlackDuckProperties blackDuckProperties;
    private final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor;

    @Autowired
    public EmailSyncTask(final TaskScheduler taskScheduler, final BlackDuckProperties blackDuckProperties, final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor) {
        super(taskScheduler, "blackduck-sync-email-task");
        this.blackDuckProperties = blackDuckProperties;
        this.blackDuckUserRepositoryAccessor = blackDuckUserRepositoryAccessor;
    }

    @Override
    public void run() {
        logger.info("### Starting email address sync operation...");
        try {
            final Set<String> currentEmailAddresses = getCurrentEmailAddresses();
            syncDBWithCurrentEmailAddresses(currentEmailAddresses);
        } catch (final IOException | IntegrationException e) {
            logger.error("Could not retrieve the current email addresses from the BlackDuck server : " + e.getMessage(), e);
        }
        logger.info("### Finished email address sync operation...");
    }

    public Set<String> getCurrentEmailAddresses() throws IOException, IntegrationException {
        final Optional<BlackduckRestConnection> optionalConnection = blackDuckProperties.createRestConnectionAndLogErrors(logger);
        if (optionalConnection.isPresent()) {
            try (final BlackduckRestConnection restConnection = optionalConnection.get()) {
                if (restConnection != null) {
                    final HubServicesFactory hubServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(restConnection, new Slf4jIntLogger(logger));
                    final HubService hubService = hubServicesFactory.createHubService();
                    final List<UserView> users = hubService.getAllResponses(ApiDiscovery.USERS_LINK_RESPONSE);
                    final Set<String> emailAddresses = users.stream().filter(userView -> StringUtils.isNotBlank(userView.email)).map(userView -> userView.email).collect(Collectors.toSet());
                    return emailAddresses;
                }
            }
        }
        return null;
    }

    public void syncDBWithCurrentEmailAddresses(final Set<String> currentEmailAddresses) {
        final Set<String> emailsToAdd = new HashSet<>();
        final Set<String> emailsToRemove = new HashSet<>();
        final List<BlackDuckUserEntity> blackDuckUserEntities = (List<BlackDuckUserEntity>) blackDuckUserRepositoryAccessor.readEntities();

        final Set<String> storedEmails = blackDuckUserEntities.stream().map(blackDuckUserEntity -> blackDuckUserEntity.getEmailAddress()).collect(Collectors.toSet());
        currentEmailAddresses.stream().forEach(currentEmailAddress -> {
            if (!storedEmails.contains(currentEmailAddress)) {
                emailsToAdd.add(currentEmailAddress);
            }
        });
        storedEmails.stream().forEach(storedEmailAddress -> {
            if (!currentEmailAddresses.contains(storedEmailAddress)) {
                emailsToRemove.add(storedEmailAddress);
            }
        });
        logger.info("Adding {} users", emailsToAdd.size());
        logger.info("Removing {} users", emailsToRemove.size());

        final List<BlackDuckUserEntity> blackDuckUserEntitiesToRemove = blackDuckUserEntities.stream()
                                                                            .filter(blackDuckUserEntity -> emailsToRemove.contains(blackDuckUserEntity.getEmailAddress()))
                                                                            .collect(Collectors.toList());
        blackDuckUserEntitiesToRemove.stream().forEach(blackDuckUserEntity -> blackDuckUserRepositoryAccessor.deleteEntity(blackDuckUserEntity.getId()));

        emailsToAdd.stream().forEach(emailToAdd ->
                                         blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity(emailToAdd, false)));
    
    }
}
