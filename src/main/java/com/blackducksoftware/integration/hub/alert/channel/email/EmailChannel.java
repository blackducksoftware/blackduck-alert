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
import java.util.HashMap;
import java.util.List;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.AlertProperties;
import com.blackducksoftware.integration.hub.alert.channel.DistributionChannel;
import com.blackducksoftware.integration.hub.alert.channel.SupportedChannels;
import com.blackducksoftware.integration.hub.alert.channel.email.model.EmailTarget;
import com.blackducksoftware.integration.hub.alert.channel.email.service.EmailMessagingService;
import com.blackducksoftware.integration.hub.alert.channel.email.service.EmailProperties;
import com.blackducksoftware.integration.hub.alert.datasource.repository.EmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.repository.EmailRepository;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;
import com.google.gson.Gson;

import freemarker.template.TemplateException;

@Component
public class EmailChannel extends DistributionChannel<EmailEvent, EmailConfigEntity> {
    private final static Logger logger = LoggerFactory.getLogger(EmailChannel.class);

    private final AlertProperties alertProperties;
    private final EmailRepository emailRepository;

    @Autowired
    public EmailChannel(final AlertProperties alertProperties, final Gson gson, final EmailRepository emailRepository) {
        super(gson, EmailEvent.class);
        this.alertProperties = alertProperties;
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
        final List<EmailConfigEntity> configurations = emailRepository.findAll();
        for (final EmailConfigEntity configuration : configurations) {
            sendMessage(emailEvent, configuration);
        }
    }

    @Override
    public void testMessage(final EmailEvent emailEvent, final EmailConfigEntity emailConfigEntity) {
        sendMessage(emailEvent, emailConfigEntity);
    }

    @Override
    public void sendMessage(final EmailEvent emailEvent, final EmailConfigEntity emailConfigEntity) {
        try {
            final EmailProperties emailProperties = new EmailProperties(emailConfigEntity);
            final EmailMessagingService emailService = new EmailMessagingService(emailProperties);

            final HashMap<String, Object> model = new HashMap<>();
            model.put(EmailProperties.TEMPLATE_KEY_SUBJECT_LINE, "Test email. Values hard coded");
            model.put(EmailProperties.TEMPLATE_KEY_EMAIL_CATEGORY, NotificationCategoryEnum.POLICY_VIOLATION.toString());
            model.put(EmailProperties.TEMPLATE_KEY_HUB_SERVER_URL, alertProperties.getHubUrl());

            final ProjectData data = emailEvent.getProjectData();
            model.put(EmailProperties.TEMPLATE_KEY_TOPIC, data);

            model.put(EmailProperties.TEMPLATE_KEY_START_DATE, String.valueOf(System.currentTimeMillis()));
            model.put(EmailProperties.TEMPLATE_KEY_END_DATE, String.valueOf(System.currentTimeMillis()));
            model.put(EmailProperties.TEMPLATE_KEY_USER_FIRST_NAME, "First");
            model.put(EmailProperties.TEMPLATE_KEY_USER_LAST_NAME, "Last Name");

            final EmailTarget emailTarget = new EmailTarget("", "digest.ftl", model);

            emailService.sendEmailMessage(emailTarget);
        } catch (final IOException | MessagingException | TemplateException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
