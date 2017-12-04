/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.channel.email;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.channel.DistributionChannel;
import com.blackducksoftware.integration.hub.alert.channel.SupportedChannels;
import com.blackducksoftware.integration.hub.alert.channel.email.model.EmailTarget;
import com.blackducksoftware.integration.hub.alert.channel.email.service.EmailMessagingService;
import com.blackducksoftware.integration.hub.alert.channel.email.service.EmailProperties;
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.EmailGroupDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.EmailGroupDistributionRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalEmailRepository;
import com.blackducksoftware.integration.hub.alert.digest.DigestTypeEnum;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.api.group.GroupRequestService;
import com.blackducksoftware.integration.hub.model.view.UserGroupView;
import com.blackducksoftware.integration.hub.model.view.UserView;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.google.gson.Gson;

import freemarker.template.TemplateException;

@Component
public class EmailChannel extends DistributionChannel<EmailEvent, GlobalEmailConfigEntity, EmailGroupDistributionConfigEntity> {
    private final static Logger logger = LoggerFactory.getLogger(EmailChannel.class);

    private final GlobalProperties globalProperties;
    private final EmailGroupDistributionRepository emailGroupDistributionRepository;

    @Autowired
    public EmailChannel(final GlobalProperties globalProperties, final Gson gson, final GlobalEmailRepository emailRepository, final CommonDistributionRepository commonDistributionRepository,
            final EmailGroupDistributionRepository emailGroupDistributionRepository) {
        super(gson, emailRepository, commonDistributionRepository, EmailEvent.class);
        this.globalProperties = globalProperties;
        this.emailGroupDistributionRepository = emailGroupDistributionRepository;
    }

    @JmsListener(destination = SupportedChannels.EMAIL)
    @Override
    public void receiveMessage(final String message) {
        super.receiveMessage(message);
    }

    @Override
    public void handleEvent(final EmailEvent emailEvent) {
        final Long eventDistributionId = emailEvent.getCommonDistributionConfigId();
        final CommonDistributionConfigEntity commonDistributionEntity = getCommonDistributionRepository().findOne(eventDistributionId);
        if (SupportedChannels.EMAIL.equals(commonDistributionEntity.getDistributionType())) {
            final Long channelDistributionConfigId = commonDistributionEntity.getDistributionConfigId();
            final EmailGroupDistributionConfigEntity channelDistributionEntity = emailGroupDistributionRepository.findOne(channelDistributionConfigId);
            sendMessage(emailEvent, channelDistributionEntity);
        } else {
            logger.warn("Recieved an email event, but it was configured for a {} event.", commonDistributionEntity.getDistributionType());
        }
    }

    @Override
    public String testMessage(final EmailGroupDistributionConfigEntity emailConfigEntity) {
        if (emailConfigEntity != null) {
            final ProjectData data = new ProjectData(DigestTypeEnum.REAL_TIME, "Test Project", "Test Version", Collections.emptyMap());
            final String emailAddress = getGlobalConfigEntity().getMailSmtpFrom();
            sendMessage(Arrays.asList(emailAddress), new EmailEvent(data, null), getGlobalConfigEntity());
            return "Attempted to send message with the given configuration.";
        }
        return null;
    }

    @Override
    public void sendMessage(final EmailEvent emailEvent, final EmailGroupDistributionConfigEntity emailConfigEntity) {
        if (emailConfigEntity != null) {
            final String hubGroupName = emailConfigEntity.getGroupName();
            try {
                final HubServicesFactory hubServicesFactory = globalProperties.createHubServicesFactory(logger);
                final List<String> emailAddresses = getEmailAddressesForGroup(hubServicesFactory.createGroupRequestService(), hubGroupName);
                sendMessage(emailAddresses, emailEvent, getGlobalConfigEntity());
            } catch (final IntegrationException e) {
                logger.error("Could not send email to {}: Could not retrieve group info from the Hub Server.", hubGroupName, e);
            }
        } else {
            logger.warn("No configuration found with id {}.", emailEvent.getCommonDistributionConfigId());
        }
    }

    public void sendMessage(final List<String> emailAddresses, final EmailEvent emailEvent, final GlobalEmailConfigEntity globalEmailConfigEntity) {
        try {
            final EmailProperties emailProperties = new EmailProperties(globalEmailConfigEntity);
            final EmailMessagingService emailService = new EmailMessagingService(emailProperties);

            final ProjectData data = emailEvent.getProjectData();

            final HashMap<String, Object> model = new HashMap<>();
            model.put(EmailProperties.TEMPLATE_KEY_SUBJECT_LINE, globalEmailConfigEntity.getEmailSubjectLine());
            model.put(EmailProperties.TEMPLATE_KEY_EMAIL_CATEGORY, data.getDigestType().getName());
            model.put(EmailProperties.TEMPLATE_KEY_HUB_SERVER_URL, StringUtils.trimToEmpty(globalProperties.hubUrl));

            model.put(EmailProperties.TEMPLATE_KEY_TOPIC, data);

            model.put(EmailProperties.TEMPLATE_KEY_START_DATE, String.valueOf(System.currentTimeMillis()));
            model.put(EmailProperties.TEMPLATE_KEY_END_DATE, String.valueOf(System.currentTimeMillis()));

            for (final String emailAddress : emailAddresses) {
                // TODO should these be individual emails, or one group email?
                final EmailTarget emailTarget = new EmailTarget(emailAddress, "digest.ftl", model);
                emailService.sendEmailMessage(emailTarget);
            }
        } catch (final IOException | MessagingException | TemplateException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private List<String> getEmailAddressesForGroup(final GroupRequestService groupRequestService, final String hubGroup) throws AlertException {
        try {
            final List<UserGroupView> groups = groupRequestService.getAllGroups();

            UserGroupView userGroupView = null;
            for (final UserGroupView group : groups) {
                if (group.name.equals(hubGroup)) {
                    userGroupView = group;
                }
            }
            return getEmailAddressesForGroup(groupRequestService, userGroupView);
        } catch (final IntegrationException e) {
            throw new AlertException(e);
        }
    }

    private List<String> getEmailAddressesForGroup(final GroupRequestService groupRequestService, final UserGroupView group) throws AlertException {
        try {
            final List<UserView> users = groupRequestService.getAllUsersForGroup(group);
            return users.stream().map(user -> user.email).collect(Collectors.toList());
        } catch (final IntegrationException e) {
            throw new AlertException(e);
        }
    }

}
