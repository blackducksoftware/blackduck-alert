/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.DistributionChannel;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.channel.email.template.EmailTarget;
import com.synopsys.integration.alert.channel.event.DistributionEvent;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.database.audit.AuditUtility;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.exception.IntegrationException;

@Component(value = EmailChannel.COMPONENT_NAME)
public class EmailChannel extends DistributionChannel {
    public static final String COMPONENT_NAME = "channel_email";
    private final BlackDuckProperties blackDuckProperties;
    private final EmailAddressHandler emailAddressHandler;

    @Autowired
    public EmailChannel(final Gson gson, final AlertProperties alertProperties, final BlackDuckProperties blackDuckProperties, final AuditUtility auditUtility, final EmailAddressHandler emailAddressHandler) {
        super(EmailChannel.COMPONENT_NAME, gson, alertProperties, auditUtility);
        this.blackDuckProperties = blackDuckProperties;
        this.emailAddressHandler = emailAddressHandler;
    }

    @Override
    public void sendMessage(final DistributionEvent event) throws IntegrationException {
        final FieldAccessor fieldAccessor = event.getFieldAccessor();

        final Optional<String> host = fieldAccessor.getString(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey());
        final Optional<String> from = fieldAccessor.getString(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey());

        if (!host.isPresent() || !from.isPresent()) {
            throw new AlertException("ERROR: Missing global config.");
        }

        final FieldAccessor updatedFieldAccessor = emailAddressHandler.updateEmailAddresses(event.getProvider(), event.getContent(), fieldAccessor);

        final Set<String> emailAddresses = updatedFieldAccessor.getAllStrings(EmailDescriptor.KEY_EMAIL_ADDRESSES).stream().collect(Collectors.toSet());
        final EmailProperties emailProperties = new EmailProperties(updatedFieldAccessor);
        final String subjectLine = fieldAccessor.getString(EmailDescriptor.KEY_SUBJECT_LINE).orElse("");
        sendMessage(emailProperties, emailAddresses, subjectLine, event.getProvider(), event.getFormatType(), event.getContent());
    }

    public void sendMessage(final EmailProperties emailProperties, final Set<String> emailAddresses, final String subjectLine, final String provider, final String formatType, final AggregateMessageContent content)
        throws IntegrationException {
        if (null == emailAddresses || emailAddresses.isEmpty()) {
            throw new AlertException("ERROR: Could not determine what email addresses to send this content to.");
        }
        try {
            final EmailMessagingService emailService = new EmailMessagingService(getAlertProperties().getAlertTemplatesDir(), emailProperties);

            final HashMap<String, Object> model = new HashMap<>();
            final Map<String, String> contentIdsToFilePaths = new HashMap<>();
            final String imagesDirectory = getAlertProperties().getAlertImagesDir();
            String templateName = "";
            if (BlackDuckProvider.COMPONENT_NAME.equals(provider)) {
                templateName = "black_duck_message_content.ftl";
                model.put(EmailPropertyKeys.EMAIL_CONTENT.getPropertyKey(), content);
                model.put(EmailPropertyKeys.EMAIL_CATEGORY.getPropertyKey(), formatType);
                model.put(EmailPropertyKeys.TEMPLATE_KEY_SUBJECT_LINE.getPropertyKey(), subjectLine);
                final Optional<String> optionalBlackDuckUrl = blackDuckProperties.getBlackDuckUrl();
                model.put(EmailPropertyKeys.TEMPLATE_KEY_BLACKDUCK_SERVER_URL.getPropertyKey(), StringUtils.trimToEmpty(optionalBlackDuckUrl.orElse("#")));
                model.put(EmailPropertyKeys.TEMPLATE_KEY_BLACKDUCK_PROJECT_NAME.getPropertyKey(), content.getValue());

                model.put(EmailPropertyKeys.TEMPLATE_KEY_START_DATE.getPropertyKey(), String.valueOf(System.currentTimeMillis()));
                model.put(EmailPropertyKeys.TEMPLATE_KEY_END_DATE.getPropertyKey(), String.valueOf(System.currentTimeMillis()));

                final String imageDirectoryPath;
                if (StringUtils.isNotBlank(imagesDirectory)) {
                    imageDirectoryPath = imagesDirectory + "/Ducky-80.png";
                } else {
                    imageDirectoryPath = System.getProperties().getProperty("user.dir") + "/src/main/resources/email/images/Ducky-80.png";
                }
                emailService.addTemplateImage(model, contentIdsToFilePaths, EmailPropertyKeys.EMAIL_LOGO_IMAGE.getPropertyKey(), imageDirectoryPath);
            } else {
                templateName = "message_content.ftl";
                model.put(EmailPropertyKeys.EMAIL_CONTENT.getPropertyKey(), content);
                model.put(EmailPropertyKeys.EMAIL_CATEGORY.getPropertyKey(), formatType);
                model.put(EmailPropertyKeys.TEMPLATE_KEY_SUBJECT_LINE.getPropertyKey(), subjectLine);
                final String imageDirectoryPath;
                if (StringUtils.isNotBlank(imagesDirectory)) {
                    imageDirectoryPath = imagesDirectory + "/synopsys.png";
                } else {
                    imageDirectoryPath = System.getProperties().getProperty("user.dir") + "/src/main/resources/email/images/synopsys.png";
                }
                emailService.addTemplateImage(model, contentIdsToFilePaths, EmailPropertyKeys.EMAIL_LOGO_IMAGE.getPropertyKey(), imageDirectoryPath);
            }

            if (!model.isEmpty() && StringUtils.isNotBlank(templateName)) {
                final EmailTarget emailTarget = new EmailTarget(emailAddresses, templateName, model, contentIdsToFilePaths);
                emailService.sendEmailMessage(emailTarget);
            }
        } catch (final IOException ex) {
            throw new AlertException(ex);
        }
    }

}
