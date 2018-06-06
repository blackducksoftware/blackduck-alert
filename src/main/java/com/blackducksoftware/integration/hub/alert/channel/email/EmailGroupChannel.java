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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditEntryRepository;
import com.blackducksoftware.integration.hub.alert.channel.DistributionChannel;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.distribution.EmailGroupDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.distribution.EmailGroupDistributionRepository;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailRepository;
import com.blackducksoftware.integration.hub.alert.channel.email.template.EmailTarget;
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.api.generated.view.UserGroupView;
import com.blackducksoftware.integration.hub.api.generated.view.UserView;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.UserGroupService;
import com.blackducksoftware.integration.rest.connection.RestConnection;
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
    public EmailGroupChannel(final Gson gson, final GlobalProperties globalProperties, final AuditEntryRepository auditEntryRepository, final GlobalEmailRepository emailRepository,
            final EmailGroupDistributionRepository emailGroupDistributionRepository, final CommonDistributionRepository commonDistributionRepository) {
        super(gson, auditEntryRepository, emailRepository, emailGroupDistributionRepository, commonDistributionRepository, EmailGroupEvent.class);

        this.globalProperties = globalProperties;
    }

    @Override
    public void sendMessage(final EmailGroupEvent event, final EmailGroupDistributionConfigEntity emailConfigEntity) throws Exception {
        if (emailConfigEntity != null) {
            final String hubGroupName = emailConfigEntity.getGroupName();
            final String subjectLine = emailConfigEntity.getEmailSubjectLine();
            final List<String> emailAddresses = getEmailAddressesForGroup(hubGroupName);
            sendMessage(emailAddresses, event, subjectLine, hubGroupName);
        } else {
            logger.warn("No configuration found with id {}.", event.getCommonDistributionConfigId());
        }
    }

    public void sendMessage(final List<String> emailAddresses, final EmailGroupEvent event, final String subjectLine, final String hubGroupName)
            throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, MessagingException, IOException, TemplateException {
        final EmailProperties emailProperties = new EmailProperties(getGlobalConfigEntity());
        final EmailMessagingService emailService = new EmailMessagingService(emailProperties);

        final Collection<ProjectData> data = event.getProjectData();

        final HashMap<String, Object> model = new HashMap<>();
        model.put(EmailProperties.TEMPLATE_KEY_SUBJECT_LINE, subjectLine);
        model.put(EmailProperties.TEMPLATE_KEY_EMAIL_CATEGORY, data.iterator().next().getDigestType().getDisplayName());
        model.put(EmailProperties.TEMPLATE_KEY_HUB_SERVER_URL, StringUtils.trimToEmpty(globalProperties.getHubUrl()));
        model.put(EmailProperties.TEMPLATE_KEY_HUB_GROUP_NAME, hubGroupName);

        model.put(EmailProperties.TEMPLATE_KEY_TOPIC, data);

        model.put(EmailProperties.TEMPLATE_KEY_START_DATE, String.valueOf(System.currentTimeMillis()));
        model.put(EmailProperties.TEMPLATE_KEY_END_DATE, String.valueOf(System.currentTimeMillis()));

        for (final String emailAddress : emailAddresses) {
            final EmailTarget emailTarget = new EmailTarget(emailAddress, "digest.ftl", model);
            emailService.sendEmailMessage(emailTarget);
        }
    }

    private List<String> getEmailAddressesForGroup(final String hubGroup) throws IntegrationException {
        try (RestConnection restConnection = globalProperties.createRestConnectionAndLogErrors(logger)) {
            if (restConnection != null) {
                final HubServicesFactory hubServicesFactory = globalProperties.createHubServicesFactory(restConnection);
                final UserGroupService groupService = hubServicesFactory.createUserGroupService();
                final UserGroupView userGroupView = groupService.getGroupByName(hubGroup);

                if (userGroupView == null) {
                    throw new IntegrationException("Could not find the Hub group: " + hubGroup);
                }
                logger.info(userGroupView.toString());
                logger.info(userGroupView.json);

                final List<UserView> users = hubServicesFactory.createHubService().getAllResponses(userGroupView, UserGroupView.USERS_LINK_RESPONSE);
                return users.stream().map(user -> user.email).collect(Collectors.toList());
            }
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }

        return Collections.emptyList();
    }
}
