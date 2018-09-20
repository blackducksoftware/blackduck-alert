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
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.DistributionChannel;
import com.synopsys.integration.alert.channel.email.template.EmailTarget;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.channel.email.EmailGlobalConfigEntity;
import com.synopsys.integration.alert.database.channel.email.EmailGlobalRepository;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.exception.IntegrationException;

@Component(value = EmailGroupChannel.COMPONENT_NAME)
public class EmailGroupChannel extends DistributionChannel<EmailGlobalConfigEntity, EmailChannelEvent> {
    public final static String COMPONENT_NAME = "channel_email";
    private final static Logger logger = LoggerFactory.getLogger(EmailGroupChannel.class);

    @Autowired
    public EmailGroupChannel(final Gson gson, final AlertProperties alertProperties, final BlackDuckProperties blackDuckProperties, final AuditEntryRepository auditEntryRepository, final EmailGlobalRepository emailRepository) {
        super(gson, alertProperties, blackDuckProperties, auditEntryRepository, emailRepository, EmailChannelEvent.class);
    }

    @Override
    public String getDistributionType() {
        return EmailGroupChannel.COMPONENT_NAME;
    }

    @Override
    public void sendMessage(final EmailChannelEvent event) throws IntegrationException {
        sendMessage(event.getEmailAddresses(), event.getSubjectLine(), event.getProvider(), event.getContent(), "ProjectName");
    }

    public void sendMessage(final Set<String> emailAddresses, final String subjectLine, final String provider, final AggregateMessageContent content, final String blackDuckProjectName) throws IntegrationException {
        final EmailGlobalConfigEntity globalConfigEntity = getGlobalConfigEntity();
        if (!isValidGlobalConfigEntity(globalConfigEntity)) {
            throw new IntegrationException("ERROR: Missing global config.");
        }
        final EmailProperties emailProperties = new EmailProperties(getGlobalConfigEntity());
        try {
            final EmailMessagingService emailService = new EmailMessagingService(getAlertProperties(), emailProperties);

            final HashMap<String, Object> model = new HashMap<>();

            final String contentTitle = provider;
            model.put("content", content);
            model.put("contentTitle", contentTitle);
            // FIXME pass in the emailCategory from the event
            model.put("emailCategory", "Aggregate");
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
        } catch (final AlertException ex) {
            logger.error("Error sending email project data from event", ex);
        }
    }

    private boolean isValidGlobalConfigEntity(final EmailGlobalConfigEntity globalConfigEntity) {
        return globalConfigEntity != null && StringUtils.isNotBlank(globalConfigEntity.getMailSmtpHost()) && StringUtils.isNotBlank(globalConfigEntity.getMailSmtpFrom());
    }

}
