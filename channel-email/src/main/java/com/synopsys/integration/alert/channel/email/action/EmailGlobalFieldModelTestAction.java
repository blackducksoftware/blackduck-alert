/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.action;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.email.distribution.EmailChannelMessageModel;
import com.synopsys.integration.alert.channel.email.distribution.EmailChannelMessagingService;
import com.synopsys.integration.alert.common.action.FieldModelTestAction;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.service.email.EmailTarget;
import com.synopsys.integration.alert.service.email.JavamailPropertiesFactory;
import com.synopsys.integration.alert.service.email.SmtpConfig;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;

@Component
public class EmailGlobalFieldModelTestAction extends FieldModelTestAction {
    private static final String TEST_SUBJECT_LINE = "Email Global Configuration Test";
    private static final String TEST_MESSAGE_CONTENT = "This is a test message from Alert to confirm your Global Email Configuration is valid.";

    private final EmailChannelMessagingService emailChannelMessagingService;
    private final JavamailPropertiesFactory javamailPropertiesFactory;

    @Autowired
    public EmailGlobalFieldModelTestAction(EmailChannelMessagingService emailChannelMessagingService, JavamailPropertiesFactory javamailPropertiesFactory) {
        this.emailChannelMessagingService = emailChannelMessagingService;
        this.javamailPropertiesFactory = javamailPropertiesFactory;
    }

    @Override
    public MessageResult testConfig(String configId, FieldModel fieldModel, FieldUtility registeredFieldValues) throws AlertException {
        String addressString = fieldModel.getFieldValue(FieldModelTestAction.KEY_DESTINATION_NAME).orElse("");
        if (StringUtils.isBlank(addressString)) {
            throw new AlertException(String.format("Could not determine what email address to send this content to. %s was not provided or was blank. Please provide a valid email address to test the configuration.", FieldModelTestAction.KEY_DESTINATION_NAME));
        }

        try {
            InternetAddress emailAddress = new InternetAddress(addressString);
            emailAddress.validate();
        } catch (AddressException ex) {
            throw new AlertException(String.format("%s is not a valid email address. %s", addressString, ex.getMessage()));
        }

        EmailChannelMessageModel testMessage = EmailChannelMessageModel.simple(TEST_SUBJECT_LINE, TEST_MESSAGE_CONTENT, "", "");

        EmailTarget emailTarget = emailChannelMessagingService.createTarget(testMessage, addressString);

        SmtpConfig smtpConfig = SmtpConfig.builder()
            .setJavamailProperties(javamailPropertiesFactory.createJavaMailProperties(registeredFieldValues))
            .setSmtpFrom(registeredFieldValues.getString(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey()).orElse(null))
            .setSmtpHost(registeredFieldValues.getString(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey()).orElse(null))
            .setSmtpPort(registeredFieldValues.getInteger(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey()).orElse(-1))
            .setSmtpAuth(registeredFieldValues.getBooleanOrFalse(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey()))
            .setSmtpUsername(registeredFieldValues.getString(EmailPropertyKeys.JAVAMAIL_USER_KEY.name()).orElse(null))
            .setSmtpPassword(registeredFieldValues.getString(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey()).orElse(null))
            .build();

        return emailChannelMessagingService.sendMessage(smtpConfig, emailTarget);
    }

}
