/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.distribution;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.channel.ChannelMessageSender;
import com.synopsys.integration.alert.channel.email.attachment.EmailAttachmentFileCreator;
import com.synopsys.integration.alert.channel.email.attachment.EmailAttachmentFormat;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.channel.email.distribution.address.EmailAddressGatherer;
import com.synopsys.integration.alert.channel.email.distribution.address.JobEmailAddressValidator;
import com.synopsys.integration.alert.channel.email.distribution.address.ValidatedEmailAddresses;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.FieldStatusSeverity;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.EmailChannelKey;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.service.email.EmailMessagingService;
import com.synopsys.integration.alert.service.email.EmailProperties;
import com.synopsys.integration.alert.service.email.EmailTarget;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.service.email.template.FreemarkerTemplatingService;

@Component
public class EmailChannelMessageSender implements ChannelMessageSender<EmailJobDetailsModel, EmailChannelMessageModel, MessageResult> {
    public static final String FILE_NAME_MESSAGE_TEMPLATE = "message_content.ftl";

    private final EmailChannelKey emailChannelKey;
    private final AlertProperties alertProperties;
    private final JobEmailAddressValidator emailAddressValidator;
    private final EmailAddressGatherer emailAddressGatherer;
    private final EmailAttachmentFileCreator emailAttachmentFileCreator;
    private final FreemarkerTemplatingService freemarkerTemplatingService;
    private final ConfigurationAccessor configurationAccessor;

    @Autowired
    public EmailChannelMessageSender(
        EmailChannelKey emailChannelKey,
        AlertProperties alertProperties,
        JobEmailAddressValidator emailAddressValidator,
        EmailAddressGatherer emailAddressGatherer,
        EmailAttachmentFileCreator emailAttachmentFileCreator,
        FreemarkerTemplatingService freemarkerTemplatingService,
        ConfigurationAccessor configurationAccessor
    ) {
        this.emailChannelKey = emailChannelKey;
        this.alertProperties = alertProperties;
        this.emailAddressValidator = emailAddressValidator;
        this.emailAddressGatherer = emailAddressGatherer;
        this.emailAttachmentFileCreator = emailAttachmentFileCreator;
        this.freemarkerTemplatingService = freemarkerTemplatingService;
        this.configurationAccessor = configurationAccessor;
    }

    @Override
    public MessageResult sendMessages(EmailJobDetailsModel emailJobDetails, List<EmailChannelMessageModel> emailMessages) throws AlertException {
        EmailProperties emailProperties = new EmailProperties(retrieveGlobalEmailConfig());
        return sendMessages(emailProperties, emailJobDetails, emailMessages);
    }

    public MessageResult sendMessages(EmailProperties emailProperties, EmailJobDetailsModel emailJobDetails, List<EmailChannelMessageModel> emailMessages) throws AlertException {
        EmailMessagingService emailMessagingService = new EmailMessagingService(emailProperties, freemarkerTemplatingService);
        EmailAttachmentFormat attachmentFormat = EmailAttachmentFormat.getValueSafely(emailJobDetails.getAttachmentFileType());

        // Validation
        ValidatedEmailAddresses validatedAdditionalEmailAddresses = validateAdditionalEmailAddresses(emailJobDetails);
        Set<String> invalidEmailAddresses = validatedAdditionalEmailAddresses.getInvalidEmailAddresses();
        if (!invalidEmailAddresses.isEmpty()) {
            emailJobDetails = new EmailJobDetailsModel(
                emailJobDetails.getJobId(),
                emailJobDetails.getSubjectLine().orElse(null),
                emailJobDetails.isProjectOwnerOnly(),
                emailJobDetails.isAdditionalEmailAddressesOnly(),
                emailJobDetails.getAttachmentFileType(),
                new ArrayList<>(validatedAdditionalEmailAddresses.getValidEmailAddresses())
            );
        }

        // Distribution
        int totalEmailsSent = 0;
        for (EmailChannelMessageModel message : emailMessages) {
            Set<String> projectHrefs = message.getSource()
                                           .map(ProjectMessage::getProject)
                                           .flatMap(LinkableItem::getUrl)
                                           .map(Set::of)
                                           .orElse(Set.of());

            Set<String> gatheredEmailAddresses = emailAddressGatherer.gatherEmailAddresses(emailJobDetails, projectHrefs);
            validateGatheredEmailAddresses(gatheredEmailAddresses, invalidEmailAddresses);
            sendMessage(emailMessagingService, attachmentFormat, message, gatheredEmailAddresses);
            totalEmailsSent += gatheredEmailAddresses.size();
        }

        // Reporting
        if (!invalidEmailAddresses.isEmpty()) {
            String invalidEmailAddressesString = StringUtils.join(invalidEmailAddresses, ", ");
            String errorMessage = String.format("No emails were sent to the following recipients because they were invalid: %s", invalidEmailAddressesString);
            AlertFieldStatus errorStatus = new AlertFieldStatus(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES, FieldStatusSeverity.ERROR, errorMessage);
            return new MessageResult(errorMessage, List.of(errorStatus));
        }
        return new MessageResult(String.format("Successfully sent %d email(s)", totalEmailsSent));
    }

    private ConfigurationModel retrieveGlobalEmailConfig() throws AlertException {
        return configurationAccessor.getConfigurationsByDescriptorKeyAndContext(emailChannelKey, ConfigContextEnum.GLOBAL)
                   .stream()
                   .findAny()
                   .orElseThrow(() -> new AlertConfigurationException("ERROR: Missing Email global config."));
    }

    private ValidatedEmailAddresses validateAdditionalEmailAddresses(EmailJobDetailsModel emailJobDetails) {
        UUID jobId = emailJobDetails.getJobId();
        if (null != jobId) {
            return emailAddressValidator.validate(jobId, emailJobDetails.getAdditionalEmailAddresses());
        }
        return new ValidatedEmailAddresses(new HashSet<>(emailJobDetails.getAdditionalEmailAddresses()), Set.of());
    }

    private void validateGatheredEmailAddresses(Set<String> gatheredEmailAddresses, Set<String> invalidEmailAddresses) throws AlertException {
        if (gatheredEmailAddresses.isEmpty()) {
            if (invalidEmailAddresses.isEmpty()) {
                throw new AlertException("Could not determine what email addresses to send this content to");
            } else {
                String invalidEmailAddressesString = StringUtils.join(invalidEmailAddresses, ", ");
                throw new AlertException(String.format("No valid email addresses to send this content to. The following email addresses were invalid: %s", invalidEmailAddressesString));
            }
        }
    }

    private void sendMessage(EmailMessagingService emailService, EmailAttachmentFormat attachmentFormat, EmailChannelMessageModel message, Set<String> emailAddresses) throws AlertException {
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
        emailService.addTemplateImage(model, contentIdsToFilePaths, EmailPropertyKeys.EMAIL_LOGO_IMAGE.getPropertyKey(), alertProperties.createSynopsysLogoPath());

        EmailTarget emailTarget = new EmailTarget(emailAddresses, FILE_NAME_MESSAGE_TEMPLATE, model, contentIdsToFilePaths);
        Optional<File> optionalAttachment = message.getSource().flatMap(projectMessage -> addAttachment(emailTarget, attachmentFormat, projectMessage));

        emailService.sendEmailMessage(emailTarget);
        optionalAttachment.ifPresent(emailAttachmentFileCreator::cleanUpAttachmentFile);
    }

    private Optional<File> addAttachment(EmailTarget emailTarget, EmailAttachmentFormat attachmentFormat, ProjectMessage projectMessage) {
        Optional<File> optionalAttachmentFile = emailAttachmentFileCreator.createAttachmentFile(attachmentFormat, projectMessage);
        if (optionalAttachmentFile.isPresent()) {
            File attachmentFile = optionalAttachmentFile.get();
            // We trust that the file was created correctly, so the path should be correct.
            emailTarget.setAttachmentFilePath(attachmentFile.getPath());
        }
        return optionalAttachmentFile;
    }

}
