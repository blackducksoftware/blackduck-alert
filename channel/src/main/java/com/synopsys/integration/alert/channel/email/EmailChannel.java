/**
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.email.template.EmailAttachmentFileCreator;
import com.synopsys.integration.alert.channel.email.template.EmailAttachmentFormat;
import com.synopsys.integration.alert.channel.email.template.EmailChannelMessageParser;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.channel.NamedDistributionChannel;
import com.synopsys.integration.alert.common.channel.template.FreemarkerTemplatingService;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditAccessor;
import com.synopsys.integration.alert.common.email.EmailMessagingService;
import com.synopsys.integration.alert.common.email.EmailProperties;
import com.synopsys.integration.alert.common.email.EmailTarget;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class EmailChannel extends NamedDistributionChannel {
    public static final String FILE_NAME_SYNOPSYS_LOGO = "synopsys.png";
    public static final String FILE_NAME_MESSAGE_TEMPLATE = "message_content.ftl";

    private final EmailAddressHandler emailAddressHandler;
    private final FreemarkerTemplatingService freemarkerTemplatingService;
    private final AlertProperties alertProperties;
    private final EmailChannelMessageParser emailChannelMessageParser;
    private final EmailAttachmentFileCreator emailAttachmentFileCreator;

    @Autowired
    public EmailChannel(Gson gson, AlertProperties alertProperties, AuditAccessor auditAccessor,
        EmailAddressHandler emailAddressHandler, FreemarkerTemplatingService freemarkerTemplatingService, EmailChannelMessageParser emailChannelMessageParser, EmailAttachmentFileCreator emailAttachmentFileCreator) {
        super(ChannelKeys.EMAIL, gson, auditAccessor);
        this.emailAddressHandler = emailAddressHandler;
        this.freemarkerTemplatingService = freemarkerTemplatingService;
        this.alertProperties = alertProperties;
        this.emailChannelMessageParser = emailChannelMessageParser;
        this.emailAttachmentFileCreator = emailAttachmentFileCreator;
    }

    @Override
    public void distributeMessage(DistributionEvent event) throws IntegrationException {
        ConfigurationModel globalConfig = event.getChannelGlobalConfig()
                                              .orElseThrow(() -> new AlertConfigurationException("ERROR: Missing global config."));
        DistributionJobModel distributionJobModel = event.getDistributionJobModel();
        EmailJobDetailsModel emailJobDetails = distributionJobModel.getDistributionJobDetails().getAs(DistributionJobDetailsModel.EMAIL);

        Optional<String> host = globalConfig.getField(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey())
                                    .flatMap(ConfigurationFieldModel::getFieldValue);
        Optional<String> from = globalConfig.getField(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey())
                                    .flatMap(ConfigurationFieldModel::getFieldValue);

        if (host.isEmpty() || from.isEmpty()) {
            throw new AlertException("ERROR: Missing global config.");
        }

        Set<String> emailAddresses = emailAddressHandler.getUpdatedEmailAddresses(event.getProviderConfigId(), event.getContent(), distributionJobModel, emailJobDetails);
        EmailProperties emailProperties = new EmailProperties(globalConfig);
        String subjectLine = emailJobDetails.getSubjectLine();
        EmailAttachmentFormat attachmentFormat = Optional.ofNullable(emailJobDetails.getAttachmentFileType())
                                                     .map(EmailAttachmentFormat::getValueSafely)
                                                     .orElse(EmailAttachmentFormat.NONE);
        sendMessage(emailProperties, emailAddresses, subjectLine, event.getProcessingType(), attachmentFormat, event.getContent());
    }

    public void sendMessage(EmailProperties emailProperties, Set<String> emailAddresses, String subjectLine, String formatType, EmailAttachmentFormat attachmentFormat, MessageContentGroup messageContent) throws IntegrationException {
        String projectName = null;
        String projectVersionName = null;
        if (!messageContent.isEmpty()) {
            projectName = messageContent.getCommonProject().getValue();
            projectVersionName = messageContent.getSubContent()
                                     .stream()
                                     .map(ProviderMessageContent::getProjectVersion)
                                     .flatMap(Optional::stream)
                                     .map(LinkableItem::getValue)
                                     .collect(Collectors.joining(", "));
        }

        String alertServerUrl = alertProperties.getServerUrl().orElse(null);
        LinkableItem commonProvider = messageContent.getCommonProvider();
        String providerName = commonProvider.getValue();
        String providerUrl = commonProvider.getUrl().orElse("#");

        if (null == emailAddresses || emailAddresses.isEmpty()) {
            String errorMessage = String.format("ERROR: Could not determine what email addresses to send this content to. Provider: %s. Topic: %s", providerName, projectName);
            throw new AlertException(errorMessage);
        }
        HashMap<String, Object> model = new HashMap<>();
        Map<String, String> contentIdsToFilePaths = new HashMap<>();

        String formattedContent = emailChannelMessageParser.createMessage(messageContent);

        model.put(EmailPropertyKeys.EMAIL_CONTENT.getPropertyKey(), formattedContent);
        model.put(EmailPropertyKeys.EMAIL_CATEGORY.getPropertyKey(), formatType);
        model.put(EmailPropertyKeys.TEMPLATE_KEY_SUBJECT_LINE.getPropertyKey(), createEnhancedSubjectLine(subjectLine, projectName, projectVersionName));
        model.put(EmailPropertyKeys.TEMPLATE_KEY_PROVIDER_URL.getPropertyKey(), providerUrl);
        model.put(EmailPropertyKeys.TEMPLATE_KEY_PROVIDER_NAME.getPropertyKey(), providerName);
        model.put(EmailPropertyKeys.TEMPLATE_KEY_PROVIDER_PROJECT_NAME.getPropertyKey(), projectName);
        model.put(EmailPropertyKeys.TEMPLATE_KEY_START_DATE.getPropertyKey(), String.valueOf(System.currentTimeMillis()));
        model.put(EmailPropertyKeys.TEMPLATE_KEY_END_DATE.getPropertyKey(), String.valueOf(System.currentTimeMillis()));
        model.put(FreemarkerTemplatingService.KEY_ALERT_SERVER_URL, alertServerUrl);

        EmailMessagingService emailService = new EmailMessagingService(emailProperties, freemarkerTemplatingService);
        emailService.addTemplateImage(model, contentIdsToFilePaths, EmailPropertyKeys.EMAIL_LOGO_IMAGE.getPropertyKey(), getImagePath(FILE_NAME_SYNOPSYS_LOGO));
        if (!model.isEmpty()) {
            EmailTarget emailTarget = new EmailTarget(emailAddresses, FILE_NAME_MESSAGE_TEMPLATE, model, contentIdsToFilePaths);
            Optional<File> optionalAttachment = addAttachment(emailTarget, attachmentFormat, messageContent);
            emailService.sendEmailMessage(emailTarget);
            optionalAttachment.ifPresent(emailAttachmentFileCreator::cleanUpAttachmentFile);
        }
    }

    private Optional<File> addAttachment(EmailTarget emailTarget, EmailAttachmentFormat attachmentFormat, MessageContentGroup messageContentGroup) {
        Optional<File> optionalAttachmentFile = emailAttachmentFileCreator.createAttachmentFile(attachmentFormat, messageContentGroup);
        if (optionalAttachmentFile.isPresent()) {
            File attachmentFile = optionalAttachmentFile.get();
            // We trust that the file was created correctly, so the path should be correct.
            emailTarget.setAttachmentFilePath(attachmentFile.getPath());
        }
        return optionalAttachmentFile;
    }

    private String createEnhancedSubjectLine(String originalSubjectLine, String providerProjectName, String providerProjectVersionName) {
        if (StringUtils.isNotBlank(providerProjectName)) {
            String subjectLine = String.format("%s | For: %s", originalSubjectLine, providerProjectName);
            if (StringUtils.isNotBlank(providerProjectVersionName)) {
                subjectLine += String.format(" %s", providerProjectVersionName);
            }
            //78 characters is the suggested length for the message: https://tools.ietf.org/html/rfc2822#section-2.1.1
            return StringUtils.abbreviate(subjectLine, 78);
        }
        return originalSubjectLine;
    }

    private String getImagePath(String imageFileName) throws AlertException {
        String imagesDirectory = alertProperties.getAlertImagesDir();
        if (StringUtils.isNotBlank(imagesDirectory)) {
            return imagesDirectory + "/" + imageFileName;
        }
        throw new AlertException(String.format("Could not find the email image directory '%s'", imagesDirectory));
    }

}
