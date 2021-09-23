/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.distribution;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.channel.ChannelMessageSender;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.email.attachment.EmailAttachmentFormat;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.channel.email.distribution.address.EmailAddressGatherer;
import com.synopsys.integration.alert.channel.email.distribution.address.JobEmailAddressValidator;
import com.synopsys.integration.alert.channel.email.distribution.address.ValidatedEmailAddresses;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.FieldStatusSeverity;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.EmailChannelKey;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.service.email.EmailTarget;
import com.synopsys.integration.alert.service.email.JavamailPropertiesFactory;
import com.synopsys.integration.alert.service.email.SmtpConfig;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;

@Component
public class EmailChannelMessageSender implements ChannelMessageSender<EmailJobDetailsModel, EmailChannelMessageModel, MessageResult> {
    public static final String FILE_NAME_MESSAGE_TEMPLATE = "message_content.ftl";

    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
    private final EmailAddressGatherer emailAddressGatherer;
    private final EmailChannelKey emailChannelKey;
    private final EmailChannelMessagingService emailChannelMessagingService;
    private final JavamailPropertiesFactory javamailPropertiesFactory;
    private final JobEmailAddressValidator emailAddressValidator;

    @Autowired
    public EmailChannelMessageSender(
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor,
        EmailAddressGatherer emailAddressGatherer,
        EmailChannelKey emailChannelKey,
        EmailChannelMessagingService emailChannelMessagingService,
        JobEmailAddressValidator emailAddressValidator,
        JavamailPropertiesFactory javamailPropertiesFactory
    ) {
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
        this.emailAddressGatherer = emailAddressGatherer;
        this.emailChannelKey = emailChannelKey;
        this.emailChannelMessagingService = emailChannelMessagingService;
        this.emailAddressValidator = emailAddressValidator;
        this.javamailPropertiesFactory = javamailPropertiesFactory;
    }

    @Override
    public MessageResult sendMessages(EmailJobDetailsModel emailJobDetails, List<EmailChannelMessageModel> emailMessages) throws AlertException {
        // Validation
        ValidatedEmailAddresses validatedAdditionalEmailAddresses;
        UUID jobId = emailJobDetails.getJobId();
        if (null != jobId) {
            validatedAdditionalEmailAddresses = emailAddressValidator.validate(jobId, emailJobDetails.getAdditionalEmailAddresses());
        } else {
            validatedAdditionalEmailAddresses = new ValidatedEmailAddresses(new HashSet<>(emailJobDetails.getAdditionalEmailAddresses()), Set.of());
        }

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
        FieldUtility globalConfiguration = configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(emailChannelKey, ConfigContextEnum.GLOBAL)
            .stream()
            .findAny()
            .map(ConfigurationModel::getCopyOfKeyToFieldMap)
            .map(FieldUtility::new)
            .orElseThrow(() -> new AlertConfigurationException("ERROR: Missing Email global config."));

        SmtpConfig smtpConfig = SmtpConfig.builder()
            .setJavamailProperties(javamailPropertiesFactory.createJavaMailProperties(globalConfiguration))
            .setSmtpFrom(globalConfiguration.getString(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey()).orElse(null))
            .setSmtpHost(globalConfiguration.getString(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey()).orElse(null))
            .setSmtpPort(globalConfiguration.getInteger(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey()).orElse(-1))
            .setSmtpAuth(globalConfiguration.getBooleanOrFalse(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey()))
            .setSmtpUsername(globalConfiguration.getString(EmailPropertyKeys.JAVAMAIL_USER_KEY.name()).orElse(null))
            .setSmtpPassword(globalConfiguration.getString(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey()).orElse(null))
            .build();

        int totalEmailsSent = 0;

        for (EmailChannelMessageModel message : emailMessages) {
            Set<String> projectHrefs = message.getSource()
                .map(ProjectMessage::getProject)
                .flatMap(LinkableItem::getUrl)
                .map(Set::of)
                .orElse(Set.of());

            Set<String> gatheredEmailAddresses = emailAddressGatherer.gatherEmailAddresses(emailJobDetails, projectHrefs);

            if (gatheredEmailAddresses.isEmpty()) {
                if (invalidEmailAddresses.isEmpty()) {
                    throw new AlertException("Could not determine what email addresses to send this content to");
                } else {
                    String invalidEmailAddressesString = StringUtils.join(invalidEmailAddresses, ", ");
                    throw new AlertException(String.format("No valid email addresses to send this content to. The following email addresses were invalid: %s", invalidEmailAddressesString));
                }
            }

            EmailTarget emailTarget = emailChannelMessagingService.createTarget(message, gatheredEmailAddresses);

            Optional<ProjectMessage> optionalProjectMessage = message.getSource();
            if (optionalProjectMessage.isPresent()) {
                EmailAttachmentFormat attachmentFormat = EmailAttachmentFormat.getValueSafely(emailJobDetails.getAttachmentFileType());
                emailChannelMessagingService.sendMessageWithAttachedProjectMessage(smtpConfig, emailTarget, optionalProjectMessage.get(), attachmentFormat);
            } else {
                emailChannelMessagingService.sendMessage(smtpConfig, emailTarget);
            }

            totalEmailsSent += emailTarget.getEmailAddresses().size();
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

}
