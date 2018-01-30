/**
 * hub-alert
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
package com.blackducksoftware.integration.hub.alert.channel.email;

import java.io.IOException;
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
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditEntryRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.channel.DistributionChannel;
import com.blackducksoftware.integration.hub.alert.channel.SupportedChannels;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.distribution.EmailGroupDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.distribution.EmailGroupDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.channel.email.template.EmailTarget;
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.api.group.GroupService;
import com.blackducksoftware.integration.hub.model.view.UserGroupView;
import com.blackducksoftware.integration.hub.model.view.UserView;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.google.gson.Gson;

import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

@Component
public class EmailGroupChannel extends DistributionChannel<EmailGroupEvent, GlobalEmailConfigEntity, EmailGroupDistributionConfigEntity> {
    private final static Logger logger = LoggerFactory.getLogger(EmailGroupChannel.class);

    private final GlobalProperties globalProperties;

    @Autowired
    public EmailGroupChannel(final GlobalProperties globalProperties, final Gson gson, final AuditEntryRepositoryWrapper auditEntryRepository, final GlobalEmailRepositoryWrapper emailRepository,
            final EmailGroupDistributionRepositoryWrapper emailGroupDistributionRepository, final CommonDistributionRepositoryWrapper commonDistributionRepository) {
        super(gson, auditEntryRepository, emailRepository, emailGroupDistributionRepository, commonDistributionRepository, EmailGroupEvent.class);

        this.globalProperties = globalProperties;
    }

    @JmsListener(destination = SupportedChannels.EMAIL_GROUP)
    @Override
    public void receiveMessage(final String message) {
        super.receiveMessage(message);
    }

    @Override
    public void sendMessage(final EmailGroupEvent event, final EmailGroupDistributionConfigEntity emailConfigEntity) throws Exception {

        if (emailConfigEntity != null) {
            final String hubGroupName = emailConfigEntity.getGroupName();
            final String subjectLine = emailConfigEntity.getEmailSubjectLine();
            final HubServicesFactory hubServicesFactory = globalProperties.createHubServicesFactory(logger);
            final List<String> emailAddresses = getEmailAddressesForGroup(hubServicesFactory.createGroupService(), hubGroupName);
            sendMessage(emailAddresses, event, subjectLine);
        } else {
            logger.warn("No configuration found with id {}.", event.getCommonDistributionConfigId());
        }
    }

    public void sendMessage(final List<String> emailAddresses, final EmailGroupEvent event, final String subjectLine)
            throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, MessagingException, IOException, TemplateException {
        final EmailProperties emailProperties = new EmailProperties(getGlobalConfigEntity());
        final EmailMessagingService emailService = new EmailMessagingService(emailProperties);

        final ProjectData data = event.getProjectData();

        final HashMap<String, Object> model = new HashMap<>();
        model.put(EmailProperties.TEMPLATE_KEY_SUBJECT_LINE, subjectLine);
        model.put(EmailProperties.TEMPLATE_KEY_EMAIL_CATEGORY, data.getDigestType().getDisplayName());
        model.put(EmailProperties.TEMPLATE_KEY_HUB_SERVER_URL, StringUtils.trimToEmpty(globalProperties.getHubUrl()));

        model.put(EmailProperties.TEMPLATE_KEY_TOPIC, data);

        model.put(EmailProperties.TEMPLATE_KEY_START_DATE, String.valueOf(System.currentTimeMillis()));
        model.put(EmailProperties.TEMPLATE_KEY_END_DATE, String.valueOf(System.currentTimeMillis()));

        for (final String emailAddress : emailAddresses) {
            final EmailTarget emailTarget = new EmailTarget(emailAddress, "digest.ftl", model);
            emailService.sendEmailMessage(emailTarget);
        }
    }

    private List<String> getEmailAddressesForGroup(final GroupService groupService, final String hubGroup) throws AlertException {
        try {
            final List<UserGroupView> groups = groupService.getAllGroups();

            UserGroupView userGroupView = null;
            for (final UserGroupView group : groups) {
                if (group.name.equals(hubGroup)) {
                    userGroupView = group;
                }
            }
            if (userGroupView == null) {
                throw new AlertException("Could not find the Hub group: " + hubGroup);
            }
            return getEmailAddressesForGroup(groupService, userGroupView);
        } catch (final AlertException e) {
            throw e;
        } catch (final IntegrationException e) {
            throw new AlertException(e);
        }
    }

    private List<String> getEmailAddressesForGroup(final GroupService groupService, final UserGroupView group) throws AlertException {
        try {
            logger.info(group.toString());
            logger.info(group.json);
            final List<UserView> users = groupService.getAllUsersForGroup(group);
            return users.stream().map(user -> user.email).collect(Collectors.toList());
        } catch (final IntegrationException e) {
            throw new AlertException(e);
        }
    }

}
