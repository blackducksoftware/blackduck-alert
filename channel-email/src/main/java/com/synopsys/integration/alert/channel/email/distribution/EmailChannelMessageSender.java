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
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.channel.ChannelMessageSender;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.channel.email.distribution.address.JobEmailAddressValidator;
import com.synopsys.integration.alert.channel.email.distribution.address.ValidatedEmailAddresses;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.FieldStatusSeverity;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.EmailChannelKey;
import com.synopsys.integration.alert.service.email.JavamailPropertiesFactory;
import com.synopsys.integration.alert.service.email.SmtpConfig;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;

@Component
public class EmailChannelMessageSender implements ChannelMessageSender<EmailJobDetailsModel, EmailChannelMessageModel, MessageResult> {
    public static final String FILE_NAME_MESSAGE_TEMPLATE = "message_content.ftl";

    private final ConfigurationAccessor configurationAccessor;
    private final EmailChannelKey emailChannelKey;
    private final EmailChannelMessagingService emailChannelMessagingService;
    private final JavamailPropertiesFactory javamailPropertiesFactory;
    private final JobEmailAddressValidator emailAddressValidator;

    @Autowired
    public EmailChannelMessageSender(
        ConfigurationAccessor configurationAccessor,
        EmailChannelKey emailChannelKey,
        EmailChannelMessagingService emailChannelMessagingService,
        JobEmailAddressValidator emailAddressValidator,
        JavamailPropertiesFactory javamailPropertiesFactory
    ) {
        this.configurationAccessor = configurationAccessor;
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
        FieldUtility globalConfiguration = configurationAccessor.getConfigurationsByDescriptorKeyAndContext(emailChannelKey, ConfigContextEnum.GLOBAL)
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

        MessageResult emailsSentSuccessfully = emailChannelMessagingService.sendMessages(smtpConfig, emailJobDetails, emailMessages, invalidEmailAddresses);

        // Reporting
        if (!invalidEmailAddresses.isEmpty()) {
            String invalidEmailAddressesString = StringUtils.join(invalidEmailAddresses, ", ");
            String errorMessage = String.format("No emails were sent to the following recipients because they were invalid: %s", invalidEmailAddressesString);
            AlertFieldStatus errorStatus = new AlertFieldStatus(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES, FieldStatusSeverity.ERROR, errorMessage);
            return new MessageResult(errorMessage, List.of(errorStatus));
        }
        return emailsSentSuccessfully;
    }

}
