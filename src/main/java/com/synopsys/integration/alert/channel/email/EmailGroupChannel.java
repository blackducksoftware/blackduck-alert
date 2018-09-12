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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.DistributionChannel;
import com.synopsys.integration.alert.channel.email.template.EmailTarget;
import com.synopsys.integration.alert.channel.event.ChannelEvent;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.channel.email.EmailGlobalConfigEntity;
import com.synopsys.integration.alert.database.channel.email.EmailGlobalRepository;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionRepository;
import com.synopsys.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelationRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.blackduck.api.generated.view.UserGroupView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.rest.BlackduckRestConnection;
import com.synopsys.integration.blackduck.service.HubServicesFactory;
import com.synopsys.integration.blackduck.service.UserGroupService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;

@Component(value = EmailGroupChannel.COMPONENT_NAME)
@Transactional
public class EmailGroupChannel extends DistributionChannel<EmailGlobalConfigEntity, EmailGroupDistributionConfigEntity> {
    public final static String COMPONENT_NAME = "channel_email";
    private final static Logger logger = LoggerFactory.getLogger(EmailGroupChannel.class);
    private final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor;
    private final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor;
    private final UserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor;

    @Autowired
    public EmailGroupChannel(final Gson gson, final AlertProperties alertProperties, final BlackDuckProperties blackDuckProperties, final AuditEntryRepository auditEntryRepository, final EmailGlobalRepository emailRepository,
        final EmailGroupDistributionRepository emailGroupDistributionRepository, final CommonDistributionRepository commonDistributionRepository, final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor,
        final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor, final UserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor) {
        super(gson, alertProperties, blackDuckProperties, auditEntryRepository, emailRepository, emailGroupDistributionRepository, commonDistributionRepository);
        this.blackDuckUserRepositoryAccessor = blackDuckUserRepositoryAccessor;
        this.blackDuckProjectRepositoryAccessor = blackDuckProjectRepositoryAccessor;
        this.userProjectRelationRepositoryAccessor = userProjectRelationRepositoryAccessor;
    }

    @Override
    public void sendMessage(final ChannelEvent event, final EmailGroupDistributionConfigEntity emailConfigEntity) throws IntegrationException {
        if (emailConfigEntity != null) {
            final String blackDuckGroupName = emailConfigEntity.getGroupName();
            final String subjectLine = emailConfigEntity.getEmailSubjectLine();
            final List<String> emailAddresses = getEmailAddressesForGroup(blackDuckGroupName);
            sendMessage(emailAddresses, event, subjectLine, blackDuckGroupName);
        } else {
            logger.warn("No configuration found with id {}.", event.getCommonDistributionConfigId());
        }
    }

    public void sendMessage(final List<String> emailAddresses, final ChannelEvent event, final String subjectLine, final String blackDuckGroupName) throws IntegrationException {
        final EmailGlobalConfigEntity globalConfigEntity = getGlobalConfigEntity();
        if (!isValidGlobalConfigEntity(globalConfigEntity)) {
            throw new IntegrationException("ERROR: Missing global config.");
        }
        final EmailProperties emailProperties = new EmailProperties(getGlobalConfigEntity());
        try {
            final EmailMessagingService emailService = new EmailMessagingService(getAlertProperties(), emailProperties);

            final HashMap<String, Object> model = new HashMap<>();

            final String contentTitle = String.format("%s -> %s", event.getProvider(), event.getNotificationType());
            final String content = event.getContent();
            model.put("content", content);
            model.put("contentTitle", contentTitle);
            model.put(EmailPropertyKeys.TEMPLATE_KEY_SUBJECT_LINE.getPropertyKey(), subjectLine);
            final Optional<String> optionalBlackDuckUrl = getBlackDuckProperties().getBlackDuckUrl();
            if (optionalBlackDuckUrl.isPresent()) {
                model.put(EmailPropertyKeys.TEMPLATE_KEY_BLACKDUCK_SERVER_URL.getPropertyKey(), StringUtils.trimToEmpty(optionalBlackDuckUrl.get()));
            }
            model.put(EmailPropertyKeys.TEMPLATE_KEY_BLACKDUCK_GROUP_NAME.getPropertyKey(), blackDuckGroupName);

            model.put(EmailPropertyKeys.TEMPLATE_KEY_START_DATE.getPropertyKey(), String.valueOf(System.currentTimeMillis()));
            model.put(EmailPropertyKeys.TEMPLATE_KEY_END_DATE.getPropertyKey(), String.valueOf(System.currentTimeMillis()));

            for (final String emailAddress : emailAddresses) {
                final EmailTarget emailTarget = new EmailTarget(emailAddress, "audit.ftl", model);
                emailService.sendEmailMessage(emailTarget);
            }
        } catch (final IOException ex) {
            throw new AlertException(ex);
        } catch (final AlertException ex) {
            logger.error("Error sending email project data from event", ex);
        }
    }

    private boolean isValidGlobalConfigEntity(final EmailGlobalConfigEntity globalConfigEntity) {
        return globalConfigEntity != null && StringUtils.isNotBlank(globalConfigEntity.getMailSmtpHost()) && StringUtils.isNotBlank(globalConfigEntity.getMailSmtpFrom());
    }

    private List<String> getEmailAddressesForGroup(final String blackDuckGroup) throws IntegrationException {
        //TODO change this to get emails for project
        //        final String projectName = "";
        //        final BlackDuckProjectEntity blackDuckProjectEntity = blackDuckProjectRepositoryAccessor.findByName(projectName);
        //        final List<UserProjectRelation> userProjectRelations = userProjectRelationRepositoryAccessor.findByBlackDuckProjectId(blackDuckProjectEntity.getId());
        //        final List<String> emailAddresses = userProjectRelations
        //                                                .stream()
        //                                                .map(userProjectRelation -> blackDuckUserRepositoryAccessor.readEntity(userProjectRelation.getBlackDuckUserId()))
        //                                                .filter(userEntity -> userEntity.isPresent())
        //                                                .map(userEntity -> ((BlackDuckUserEntity) userEntity.get()).getEmailAddress())
        //                                                .collect(Collectors.toList());

        final Optional<BlackduckRestConnection> optionalRestConnection = getBlackDuckProperties().createRestConnectionAndLogErrors(logger);
        if (optionalRestConnection.isPresent()) {
            try (final BlackduckRestConnection restConnection = optionalRestConnection.get()) {
                if (restConnection != null) {
                    final HubServicesFactory blackDuckServicesFactory = getBlackDuckProperties().createBlackDuckServicesFactory(restConnection, new Slf4jIntLogger(logger));
                    final UserGroupService groupService = blackDuckServicesFactory.createUserGroupService();
                    final UserGroupView userGroupView = groupService.getGroupByName(blackDuckGroup);

                    if (userGroupView == null) {
                        throw new IntegrationException("Could not find the Black Duck group: " + blackDuckGroup);
                    }

                    logger.debug("Current user groups {}", userGroupView.toString());

                    final List<UserView> users = blackDuckServicesFactory.createHubService().getAllResponses(userGroupView, UserGroupView.USERS_LINK_RESPONSE);
                    return users.stream().map(user -> user.email).collect(Collectors.toList());
                }
            } catch (final IOException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            logger.warn("Could not get the email addresses for this group, could not create the connection.");
        }
        return Collections.emptyList();
    }

}
