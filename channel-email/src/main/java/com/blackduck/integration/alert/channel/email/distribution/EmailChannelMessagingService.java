/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.email.distribution;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectMessage;
import com.blackduck.integration.alert.channel.email.attachment.EmailAttachmentFileCreator;
import com.blackduck.integration.alert.channel.email.attachment.EmailAttachmentFormat;
import com.blackduck.integration.alert.common.AlertProperties;
import com.blackduck.integration.alert.common.message.model.MessageResult;
import com.blackduck.integration.alert.service.email.EmailMessagingService;
import com.blackduck.integration.alert.service.email.EmailTarget;
import com.blackduck.integration.alert.service.email.SmtpConfig;
import com.blackduck.integration.alert.service.email.enumeration.EmailPropertyKeys;
import com.blackduck.integration.alert.service.email.template.FreemarkerTemplatingService;

@Component
public class EmailChannelMessagingService {
    private final AlertProperties alertProperties;
    private final EmailMessagingService emailMessagingService;
    private final EmailAttachmentFileCreator emailAttachmentFileCreator;

    @Autowired
    public EmailChannelMessagingService(AlertProperties alertProperties, EmailMessagingService emailMessagingService, EmailAttachmentFileCreator emailAttachmentFileCreator) {
        this.alertProperties = alertProperties;
        this.emailMessagingService = emailMessagingService;
        this.emailAttachmentFileCreator = emailAttachmentFileCreator;
    }

    public MessageResult sendMessage(SmtpConfig smtpConfig, EmailTarget emailTarget) throws AlertException {
        return sendMessageWithAttachedProjectMessage(smtpConfig, emailTarget, null, EmailAttachmentFormat.NONE);
    }

    public MessageResult sendMessageWithAttachedProjectMessage(
        SmtpConfig smtpConfig,
        EmailTarget emailTarget,
        ProjectMessage message,
        EmailAttachmentFormat attachmentFormat
    ) throws AlertException {
        sendMessageWithAttachmentAndCleanUp(smtpConfig, emailTarget, message, attachmentFormat);

        return new MessageResult(String.format("Successfully sent %d email(s)", emailTarget.getEmailAddresses().size()));
    }

    public EmailTarget createTarget(EmailChannelMessageModel message, String... validatedEmailAddresses) throws AlertException {
        return createTarget(message, Set.of(validatedEmailAddresses));
    }

    public EmailTarget createTarget(EmailChannelMessageModel message, Set<String> gatheredEmailAddresses) throws AlertException {
        HashMap<String, Object> model = new HashMap<>();
        model.put(EmailPropertyKeys.EMAIL_CONTENT.getPropertyKey(), message.getContent());
        model.put(EmailPropertyKeys.EMAIL_CATEGORY.getPropertyKey(), message.getMessageFormat());
        model.put(EmailPropertyKeys.TEMPLATE_KEY_SUBJECT_LINE.getPropertyKey(), message.getSubjectLine());
        model.put(EmailPropertyKeys.TEMPLATE_KEY_PROVIDER_URL.getPropertyKey(), message.getProviderUrl());
        model.put(EmailPropertyKeys.TEMPLATE_KEY_PROVIDER_NAME.getPropertyKey(), message.getProviderName());
        model.put(EmailPropertyKeys.TEMPLATE_KEY_PROVIDER_PROJECT_NAME.getPropertyKey(), message.getProjectName().orElse(null));
        model.put(EmailPropertyKeys.TEMPLATE_KEY_START_DATE.getPropertyKey(), String.valueOf(System.currentTimeMillis()));
        model.put(EmailPropertyKeys.TEMPLATE_KEY_END_DATE.getPropertyKey(), String.valueOf(System.currentTimeMillis()));
        model.put(FreemarkerTemplatingService.KEY_ALERT_SERVER_URL, alertProperties.getServerURL());

        Map<String, String> contentIdsToFilePaths = new HashMap<>();
        emailMessagingService.addTemplateImage(model, contentIdsToFilePaths, EmailPropertyKeys.EMAIL_LOGO_IMAGE.getPropertyKey(), alertProperties.createBlackDuckLogoPath());

        return new EmailTarget(gatheredEmailAddresses, EmailChannelMessageSender.FILE_NAME_MESSAGE_TEMPLATE, model, contentIdsToFilePaths);
    }

    private void sendMessageWithAttachmentAndCleanUp(
        SmtpConfig smtpConfig,
        EmailTarget emailTarget,
        ProjectMessage projectMessage,
        EmailAttachmentFormat attachmentFormat
    ) throws AlertException {
        Optional<File> optionalAttachmentFile = Optional.empty();

        if (projectMessage != null) {
            optionalAttachmentFile = emailAttachmentFileCreator.createAttachmentFile(attachmentFormat, projectMessage);
            if (optionalAttachmentFile.isPresent()) {
                File attachmentFile = optionalAttachmentFile.get();
                // We trust that the file was created correctly, so the path should be correct.
                emailTarget.setAttachmentFilePath(attachmentFile.getPath());
            }
        }

        emailMessagingService.sendEmailMessage(
            smtpConfig.getJavamailProperties(),
            smtpConfig.getSmtpFrom(),
            smtpConfig.getSmtpHost(),
            smtpConfig.getSmtpPort(),
            smtpConfig.isSmtpAuth(),
            smtpConfig.getSmtpUsername(),
            smtpConfig.getSmtpPassword(),
            emailTarget
        );

        optionalAttachmentFile.ifPresent(emailAttachmentFileCreator::cleanUpAttachmentFile);
    }

}
