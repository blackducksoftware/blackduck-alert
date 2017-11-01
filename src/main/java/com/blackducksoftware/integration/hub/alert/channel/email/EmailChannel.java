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
import java.util.Collections;
import java.util.HashMap;

import javax.mail.MessagingException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.channel.DistributionChannel;
import com.blackducksoftware.integration.hub.alert.channel.SupportedChannels;
import com.blackducksoftware.integration.hub.alert.channel.email.model.EmailTarget;
import com.blackducksoftware.integration.hub.alert.channel.email.service.EmailMessagingService;
import com.blackducksoftware.integration.hub.alert.channel.email.service.EmailProperties;
import com.blackducksoftware.integration.hub.alert.datasource.entity.EmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.relation.UserConfigRelation;
import com.blackducksoftware.integration.hub.alert.datasource.repository.EmailRepository;
import com.blackducksoftware.integration.hub.alert.datasource.repository.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.datasource.repository.UserRelationRepository;
import com.blackducksoftware.integration.hub.alert.digest.DigestTypeEnum;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.google.gson.Gson;

import freemarker.template.TemplateException;

@Component
public class EmailChannel extends DistributionChannel<EmailEvent, EmailConfigEntity> {
    private final static Logger logger = LoggerFactory.getLogger(EmailChannel.class);

    private final GlobalProperties globalProperties;
    private final EmailRepository emailRepository;
    private final UserRelationRepository userRelationRepository;

    @Autowired
    public EmailChannel(final GlobalProperties globalProperties, final Gson gson, final UserRelationRepository userRelationRepository, final EmailRepository emailRepository) {
        super(gson, EmailEvent.class);
        this.globalProperties = globalProperties;
        this.userRelationRepository = userRelationRepository;
        this.emailRepository = emailRepository;
    }

    @JmsListener(destination = SupportedChannels.EMAIL)
    @Override
    public void receiveMessage(final String message) {
        logger.info("Received email event message: {}", message);
        final EmailEvent emailEvent = getEvent(message);
        logger.info("Email event {}", emailEvent);
        handleEvent(emailEvent);
    }

    private void handleEvent(final EmailEvent emailEvent) {
        final UserConfigRelation relationRow = userRelationRepository.findChannelConfig(emailEvent.getUserConfigId(), SupportedChannels.EMAIL);
        final Long configId = relationRow.getChannelConfigId();
        final EmailConfigEntity configuration = emailRepository.findOne(configId);
        sendMessage(emailEvent, configuration);
    }

    @Override
    public String testMessage(final EmailConfigEntity emailConfigEntity) {
        if (emailConfigEntity != null) {
            final ProjectData data = new ProjectData(DigestTypeEnum.REAL_TIME, "Test Project", "Test Version", Collections.emptyMap());
            sendMessage(emailConfigEntity.getMailSmtpFrom(), new EmailEvent(data, null), emailConfigEntity);
            return "Attempted to send message with the given configuration.";
        }
        return null;
    }

    @Override
    public void sendMessage(final EmailEvent emailEvent, final EmailConfigEntity emailConfigEntity) {
        // TODO get the emails from the hub from the config
        sendMessage("", emailEvent, emailConfigEntity);
    }

    public void sendMessage(final String emailAddress, final EmailEvent emailEvent, final EmailConfigEntity emailConfigEntity) {
        try {
            final EmailProperties emailProperties = new EmailProperties(emailConfigEntity);
            final EmailMessagingService emailService = new EmailMessagingService(emailProperties);

            final ProjectData data = emailEvent.getProjectData();

            final HashMap<String, Object> model = new HashMap<>();
            model.put(EmailProperties.TEMPLATE_KEY_SUBJECT_LINE, emailConfigEntity.getEmailSubjectLine());
            model.put(EmailProperties.TEMPLATE_KEY_EMAIL_CATEGORY, data.getDigestType().getName());
            model.put(EmailProperties.TEMPLATE_KEY_HUB_SERVER_URL, StringUtils.trimToEmpty(globalProperties.getHubUrl()));

            model.put(EmailProperties.TEMPLATE_KEY_TOPIC, data);

            model.put(EmailProperties.TEMPLATE_KEY_START_DATE, String.valueOf(System.currentTimeMillis()));
            model.put(EmailProperties.TEMPLATE_KEY_END_DATE, String.valueOf(System.currentTimeMillis()));

            final EmailTarget emailTarget = new EmailTarget(emailAddress, "digest.ftl", model);

            emailService.sendEmailMessage(emailTarget);
        } catch (final IOException | MessagingException | TemplateException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
