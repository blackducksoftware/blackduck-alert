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
package com.blackducksoftware.integration.alert.channel.email;

import java.io.IOException;
import java.util.Collection;
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

import com.blackducksoftware.integration.alert.channel.DistributionChannel;
import com.blackducksoftware.integration.alert.channel.email.template.EmailTarget;
import com.blackducksoftware.integration.alert.channel.event.ChannelEvent;
import com.blackducksoftware.integration.alert.common.AlertProperties;
import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.digest.model.DigestModel;
import com.blackducksoftware.integration.alert.common.digest.model.ProjectData;
import com.blackducksoftware.integration.alert.common.enumeration.EmailPropertyKeys;
import com.blackducksoftware.integration.alert.common.exception.AlertException;
import com.blackducksoftware.integration.alert.database.audit.AuditEntryRepository;
import com.blackducksoftware.integration.alert.database.channel.email.EmailGlobalConfigEntity;
import com.blackducksoftware.integration.alert.database.channel.email.EmailGlobalRepository;
import com.blackducksoftware.integration.alert.database.channel.email.EmailGroupDistributionConfigEntity;
import com.blackducksoftware.integration.alert.database.channel.email.EmailGroupDistributionRepository;
import com.blackducksoftware.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.view.UserGroupView;
import com.blackducksoftware.integration.hub.api.generated.view.UserView;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.UserGroupService;
import com.blackducksoftware.integration.rest.connection.RestConnection;
import com.google.gson.Gson;

@Component(value = EmailGroupChannel.COMPONENT_NAME)
@Transactional
public class EmailGroupChannel extends DistributionChannel<EmailGlobalConfigEntity, EmailGroupDistributionConfigEntity> {
    public final static String COMPONENT_NAME = "channel_email";
    private final static Logger logger = LoggerFactory.getLogger(EmailGroupChannel.class);

    @Autowired
    public EmailGroupChannel(final Gson gson, final AlertProperties alertProperties, final BlackDuckProperties blackDuckProperties, final AuditEntryRepository auditEntryRepository, final EmailGlobalRepository emailRepository,
            final EmailGroupDistributionRepository emailGroupDistributionRepository, final CommonDistributionRepository commonDistributionRepository, final ContentConverter contentExtractor) {
        super(gson, alertProperties, blackDuckProperties, auditEntryRepository, emailRepository, emailGroupDistributionRepository, commonDistributionRepository, contentExtractor);
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
        final EmailProperties emailProperties = new EmailProperties(getGlobalConfigEntity());
        try {
            final EmailMessagingService emailService = new EmailMessagingService(getAlertProperties(), emailProperties);
            final Optional<DigestModel> optionalModel = extractContentFromEvent(event, DigestModel.class);
            final Collection<ProjectData> data;
            if (optionalModel.isPresent()) {
                data = optionalModel.get().getProjectDataCollection();
            } else {
                data = Collections.emptyList();
            }

            final HashMap<String, Object> model = new HashMap<>();

            model.put(EmailPropertyKeys.TEMPLATE_KEY_SUBJECT_LINE.getPropertyKey(), subjectLine);
            model.put(EmailPropertyKeys.TEMPLATE_KEY_EMAIL_CATEGORY.getPropertyKey(), data.iterator().next().getDigestType().getDisplayName());
            final Optional<String> optionalBlackDuckUrl = getBlackDuckProperties().getBlackDuckUrl();
            if (optionalBlackDuckUrl.isPresent()) {
                model.put(EmailPropertyKeys.TEMPLATE_KEY_BLACKDUCK_SERVER_URL.getPropertyKey(), StringUtils.trimToEmpty(optionalBlackDuckUrl.get()));
            }
            model.put(EmailPropertyKeys.TEMPLATE_KEY_BLACKDUCK_GROUP_NAME.getPropertyKey(), blackDuckGroupName);

            model.put(EmailPropertyKeys.TEMPLATE_KEY_TOPIC.getPropertyKey(), data);

            model.put(EmailPropertyKeys.TEMPLATE_KEY_START_DATE.getPropertyKey(), String.valueOf(System.currentTimeMillis()));
            model.put(EmailPropertyKeys.TEMPLATE_KEY_END_DATE.getPropertyKey(), String.valueOf(System.currentTimeMillis()));

            for (final String emailAddress : emailAddresses) {
                final EmailTarget emailTarget = new EmailTarget(emailAddress, "digest.ftl", model);
                emailService.sendEmailMessage(emailTarget);
            }
        } catch (final IOException ex) {
            throw new AlertException(ex);
        } catch (final AlertException ex) {
            logger.error("Error sending email project data from event", ex);
        }
    }

    private List<String> getEmailAddressesForGroup(final String blackDuckGroup) throws IntegrationException {
        final Optional<RestConnection> optionalRestConnection = getBlackDuckProperties().createRestConnectionAndLogErrors(logger);
        if (optionalRestConnection.isPresent()) {
            try (final RestConnection restConnection = optionalRestConnection.get()) {
                if (restConnection != null) {
                    final HubServicesFactory blackDuckServicesFactory = getBlackDuckProperties().createBlackDuckServicesFactory(restConnection);
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
