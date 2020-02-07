/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.channel.email.template.EmailTarget;
import com.synopsys.integration.alert.channel.util.FreemarkerTemplatingService;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.channel.DistributionChannel;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.database.api.DefaultAuditUtility;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.polaris.PolarisProperties;
import com.synopsys.integration.exception.IntegrationException;

@Component(value = EmailChannel.COMPONENT_NAME)
public class EmailChannel extends DistributionChannel {
    public static final String COMPONENT_NAME = "channel_email";

    public static final String PROPERTY_USER_DIR = "user.dir";
    public static final String FILE_NAME_SYNOPSYS_LOGO = "synopsys.png";
    public static final String FILE_NAME_MESSAGE_TEMPLATE = "message_content.ftl";
    public static final String DIRECTORY_EMAIL_IMAGE_RESOURCES = "/src/main/resources/email/images/";

    private final BlackDuckProperties blackDuckProperties;
    private final PolarisProperties polarisProperties;
    private final EmailAddressHandler emailAddressHandler;
    private final FreemarkerTemplatingService freemarkerTemplatingService;
    private final AlertProperties alertProperties;

    @Autowired
    public EmailChannel(final Gson gson, final AlertProperties alertProperties, final BlackDuckProperties blackDuckProperties, final PolarisProperties polarisProperties, final DefaultAuditUtility auditUtility,
        final EmailAddressHandler emailAddressHandler, final FreemarkerTemplatingService freemarkerTemplatingService) {
        super(gson, auditUtility);
        this.blackDuckProperties = blackDuckProperties;
        this.polarisProperties = polarisProperties;
        this.emailAddressHandler = emailAddressHandler;
        this.freemarkerTemplatingService = freemarkerTemplatingService;
        this.alertProperties = alertProperties;
    }

    @Override
    public String sendMessage(final DistributionEvent event) throws IntegrationException {
        final FieldAccessor fieldAccessor = event.getFieldAccessor();

        final Optional<String> host = fieldAccessor.getString(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey());
        final Optional<String> from = fieldAccessor.getString(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey());

        if (!host.isPresent() || !from.isPresent()) {
            throw new AlertException("ERROR: Missing global config.");
        }

        // FIXME this should update addresses based on Provider event.getProvider()
        final FieldAccessor updatedFieldAccessor = emailAddressHandler.updateEmailAddresses(event.getContent(), fieldAccessor);

        final Set<String> emailAddresses = updatedFieldAccessor.getAllStrings(EmailDescriptor.KEY_EMAIL_ADDRESSES).stream().collect(Collectors.toSet());
        final EmailProperties emailProperties = new EmailProperties(updatedFieldAccessor);
        final String subjectLine = fieldAccessor.getString(EmailDescriptor.KEY_SUBJECT_LINE).orElse("");
        return sendMessage(emailProperties, emailAddresses, subjectLine, event.getFormatType(), event.getContent());
    }

    public String sendMessage(final EmailProperties emailProperties, final Set<String> emailAddresses, final String subjectLine, final String formatType, final MessageContentGroup content)
        throws IntegrationException {
        String topicValue = null;
        if (!content.isEmpty()) {
            topicValue = content.getCommonTopic().getValue();
        }

        final String alertServerUrl = alertProperties.getServerUrl().orElse(null);
        final LinkableItem comonProvider = content.getComonProvider();
        final String providerName = comonProvider.getValue();
        final String providerUrl = comonProvider.getUrl().orElse("#");

        if (null == emailAddresses || emailAddresses.isEmpty()) {
            final String errorMessage = String.format("ERROR: Could not determine what email addresses to send this content to. Provider: %s. Topic: %s", providerName, topicValue);
            throw new AlertException(errorMessage);
        }
        final HashMap<String, Object> model = new HashMap<>();
        final Map<String, String> contentIdsToFilePaths = new HashMap<>();

        model.put(EmailPropertyKeys.EMAIL_CONTENT.getPropertyKey(), content);
        model.put(EmailPropertyKeys.EMAIL_CATEGORY.getPropertyKey(), formatType);
        model.put(EmailPropertyKeys.TEMPLATE_KEY_SUBJECT_LINE.getPropertyKey(), createEnhancedSubjectLine(subjectLine, topicValue));
        model.put(EmailPropertyKeys.TEMPLATE_KEY_PROVIDER_URL.getPropertyKey(), providerUrl);
        model.put(EmailPropertyKeys.TEMPLATE_KEY_PROVIDER_NAME.getPropertyKey(), providerName);
        model.put(EmailPropertyKeys.TEMPLATE_KEY_PROVIDER_PROJECT_NAME.getPropertyKey(), topicValue);
        model.put(EmailPropertyKeys.TEMPLATE_KEY_START_DATE.getPropertyKey(), String.valueOf(System.currentTimeMillis()));
        model.put(EmailPropertyKeys.TEMPLATE_KEY_END_DATE.getPropertyKey(), String.valueOf(System.currentTimeMillis()));
        model.put(FreemarkerTemplatingService.KEY_ALERT_SERVER_URL, alertServerUrl);

        final EmailMessagingService emailService = new EmailMessagingService(emailProperties, freemarkerTemplatingService);
        emailService.addTemplateImage(model, contentIdsToFilePaths, EmailPropertyKeys.EMAIL_LOGO_IMAGE.getPropertyKey(), getImagePath(FILE_NAME_SYNOPSYS_LOGO));
        if (!model.isEmpty()) {
            final EmailTarget emailTarget = new EmailTarget(emailAddresses, FILE_NAME_MESSAGE_TEMPLATE, model, contentIdsToFilePaths);
            emailService.sendEmailMessage(emailTarget);
        }
        return "Successfully sent Email message";
    }

    private String createEnhancedSubjectLine(final String originalSubjectLine, final String providerProjectName) {
        if (StringUtils.isNotBlank(providerProjectName)) {
            return String.format("%s | For: %s", originalSubjectLine, providerProjectName);
        }
        return originalSubjectLine;
    }

    private String getImagePath(final String imageFileName) {
        final String imagesDirectory = alertProperties.getAlertImagesDir();
        if (StringUtils.isNotBlank(imagesDirectory)) {
            return imagesDirectory + "/" + imageFileName;
        }
        final String userDirectory = System.getProperties().getProperty(PROPERTY_USER_DIR);
        return userDirectory + DIRECTORY_EMAIL_IMAGE_RESOURCES + imageFileName;
    }

    @Override
    public String getDestinationName() {
        return COMPONENT_NAME;
    }
}
