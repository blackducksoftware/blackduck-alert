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
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.DistributionChannel;
import com.synopsys.integration.alert.channel.email.template.EmailTarget;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.database.audit.AuditUtility;
import com.synopsys.integration.alert.database.channel.email.EmailGlobalConfigEntity;
import com.synopsys.integration.alert.database.channel.email.EmailGlobalRepository;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.web.channel.model.EmailGlobalConfig;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.exception.IntegrationException;

@Component(value = EmailGroupChannel.COMPONENT_NAME)
public class EmailGroupChannel extends DistributionChannel<EmailGlobalConfigEntity, EmailChannelEvent> {
    public final static String COMPONENT_NAME = "channel_email";

    @Autowired
    public EmailGroupChannel(final Gson gson, final AlertProperties alertProperties, final BlackDuckProperties blackDuckProperties, final AuditUtility auditUtility, final EmailGlobalRepository emailRepository) {
        super(gson, alertProperties, blackDuckProperties, auditUtility, emailRepository, EmailChannelEvent.class);
    }

    @Override
    public String getDistributionType() {
        return EmailGroupChannel.COMPONENT_NAME;
    }

    @Override
    public void sendMessage(final EmailChannelEvent event) throws IntegrationException {
        final EmailGlobalConfigEntity globalConfigEntity = getGlobalConfigEntity();
        if (!isValidGlobalConfigEntity(globalConfigEntity)) {
            throw new IntegrationException("ERROR: Missing global config.");
        }
        final EmailProperties emailProperties = new EmailProperties(globalConfigEntity);
        sendMessage(emailProperties, event.getEmailAddresses(), event.getSubjectLine(), event.getProvider(), event.getFormatType(), event.getContent(), "ProjectName");
    }

    @Override
    public String testGlobalConfig(final Config restModel, final String testEmailAddress) throws IntegrationException {
        Set<String> emailAddresses = null;
        if (StringUtils.isNotBlank(testEmailAddress)) {
            emailAddresses = Collections.singleton(testEmailAddress);
        }
        final EmailProperties globalConfigEntity = new EmailProperties((EmailGlobalConfig) restModel);
        final AggregateMessageContent messageContent = new AggregateMessageContent("Message Content", "Test from Alert", Collections.emptyList());
        sendMessage(globalConfigEntity, emailAddresses, "Test from Alert", "Global Configuration", "", messageContent, "N/A");
        return "Success!";
    }

    public void sendMessage(final EmailProperties emailProperties, final Set<String> emailAddresses, final String subjectLine, final String provider, final String formatType, final AggregateMessageContent content,
            final String blackDuckProjectName) throws IntegrationException {
        if (null == emailAddresses || emailAddresses.isEmpty()) {
            throw new IntegrationException("ERROR: Could not determine what email addresses to send this content to.");
        }
        try {
            final EmailMessagingService emailService = new EmailMessagingService(getAlertProperties(), emailProperties);

            final HashMap<String, Object> model = new HashMap<>();

            final String contentTitle = provider;
            model.put("content", content);
            model.put("contentTitle", contentTitle);
            model.put("emailCategory", formatType);
            model.put(EmailPropertyKeys.TEMPLATE_KEY_SUBJECT_LINE.getPropertyKey(), subjectLine);
            final Optional<String> optionalBlackDuckUrl = getBlackDuckProperties().getBlackDuckUrl();
            if (optionalBlackDuckUrl.isPresent()) {
                model.put(EmailPropertyKeys.TEMPLATE_KEY_BLACKDUCK_SERVER_URL.getPropertyKey(), StringUtils.trimToEmpty(optionalBlackDuckUrl.get()));
            }
            model.put(EmailPropertyKeys.TEMPLATE_KEY_BLACKDUCK_PROJECT_NAME.getPropertyKey(), blackDuckProjectName);

            model.put(EmailPropertyKeys.TEMPLATE_KEY_START_DATE.getPropertyKey(), String.valueOf(System.currentTimeMillis()));
            model.put(EmailPropertyKeys.TEMPLATE_KEY_END_DATE.getPropertyKey(), String.valueOf(System.currentTimeMillis()));

            for (final String emailAddress : emailAddresses) {
                final EmailTarget emailTarget = new EmailTarget(emailAddress, "message_content.ftl", model);
                emailService.sendEmailMessage(emailTarget);
            }
        } catch (final IOException ex) {
            throw new AlertException(ex);
        }
    }

    private boolean isValidGlobalConfigEntity(final EmailGlobalConfigEntity globalConfigEntity) {
        return globalConfigEntity != null && StringUtils.isNotBlank(globalConfigEntity.getMailSmtpHost()) && StringUtils.isNotBlank(globalConfigEntity.getMailSmtpFrom());
    }

}
