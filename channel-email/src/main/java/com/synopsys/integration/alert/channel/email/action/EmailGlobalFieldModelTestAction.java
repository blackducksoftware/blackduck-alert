/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.action;

import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.email.attachment.EmailAttachmentFormat;
import com.synopsys.integration.alert.channel.email.distribution.EmailChannelMessageModel;
import com.synopsys.integration.alert.channel.email.distribution.EmailChannelMessageSender;
import com.synopsys.integration.alert.common.action.FieldModelTestAction;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.service.email.JavamailPropertiesFactory;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;

@Component
public class EmailGlobalFieldModelTestAction extends FieldModelTestAction {
    private static final String TEST_SUBJECT_LINE = "Email Global Configuration Test";
    private static final String TEST_MESSAGE_CONTENT = "This is a test message from Alert to confirm your Global Email Configuration is valid.";

    private final EmailChannelMessageSender emailChannelMessageSender;
    private final JavamailPropertiesFactory javamailPropertiesFactory;

    @Autowired
    public EmailGlobalFieldModelTestAction(EmailChannelMessageSender emailChannelMessageSender, JavamailPropertiesFactory javamailPropertiesFactory) {
        this.emailChannelMessageSender = emailChannelMessageSender;
        this.javamailPropertiesFactory = javamailPropertiesFactory;
    }

    @Override
    public MessageResult testConfig(String configId, FieldModel fieldModel, FieldUtility registeredFieldValues) throws AlertException {
        List<String> emailAddresses = validateAndWrapDestinationAsList(fieldModel.getFieldValue(FieldModelTestAction.KEY_DESTINATION_NAME).orElse(""));
        EmailJobDetailsModel distributionDetails = new EmailJobDetailsModel(
            null,
            javamailPropertiesFactory.createJavaMailProperties(registeredFieldValues),
            registeredFieldValues.getStringOrEmpty(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey()),
            registeredFieldValues.getStringOrEmpty(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey()),
            registeredFieldValues.getInteger(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey()).orElse(0),
            registeredFieldValues.getBooleanOrFalse(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey()),
            registeredFieldValues.getStringOrEmpty(EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey()),
            registeredFieldValues.getStringOrEmpty(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey()),
            TEST_SUBJECT_LINE,
            false,
            true,
            EmailAttachmentFormat.NONE.name(),
            emailAddresses
        );

        EmailChannelMessageModel testMessage = EmailChannelMessageModel.simple(TEST_SUBJECT_LINE, TEST_MESSAGE_CONTENT, "", "");

        return emailChannelMessageSender.sendMessages(distributionDetails, List.of(testMessage));
    }

    private List<String> validateAndWrapDestinationAsList(String addressString) throws AlertException {
        if (StringUtils.isNotBlank(addressString)) {
            try {
                InternetAddress emailAddress = new InternetAddress(addressString);
                emailAddress.validate();
            } catch (AddressException ex) {
                throw new AlertException(String.format("%s is not a valid email address. %s", addressString, ex.getMessage()));
            }
            return List.of(addressString);
        }
        return List.of();
    }

}
