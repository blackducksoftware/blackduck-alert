/*
 * channel
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

import com.synopsys.integration.alert.channel.email.attachment.EmailAttachmentFormat;
import com.synopsys.integration.alert.channel.email.distribution.EmailChannelMessageModel;
import com.synopsys.integration.alert.channel.email.distribution.EmailChannelMessageSender;
import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.email.EmailProperties;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;

@Component
public class EmailGlobalTestAction extends TestAction {
    private static final String TEST_SUBJECT_LINE = "Email Global Configuration Test";
    private static final String TEST_MESSAGE_CONTENT = "This is a test message from Alert to confirm your Global Email Configuration is valid.";

    private final EmailChannelMessageSender emailChannelMessageSender;

    @Autowired
    public EmailGlobalTestAction(EmailChannelMessageSender emailChannelMessageSender) {
        this.emailChannelMessageSender = emailChannelMessageSender;
    }

    @Override
    public MessageResult testConfig(String configId, FieldModel fieldModel, FieldUtility registeredFieldValues) throws AlertException {
        EmailProperties emailProperties = new EmailProperties(registeredFieldValues);

        List<String> emailAddresses = extractAndValidateDestination(fieldModel);
        EmailJobDetailsModel distributionDetails = new EmailJobDetailsModel(null, TEST_SUBJECT_LINE, false, true, EmailAttachmentFormat.NONE.name(), emailAddresses);

        EmailChannelMessageModel testMessage = EmailChannelMessageModel.simple(TEST_SUBJECT_LINE, TEST_MESSAGE_CONTENT, "", "");

        return emailChannelMessageSender.sendMessages(emailProperties, distributionDetails, List.of(testMessage));
    }

    private List<String> extractAndValidateDestination(FieldModel fieldModel) throws AlertException {
        String destination = fieldModel.getFieldValue(TestAction.KEY_DESTINATION_NAME).orElse("");
        if (StringUtils.isNotBlank(destination)) {
            try {
                InternetAddress emailAddr = new InternetAddress(destination);
                emailAddr.validate();
            } catch (AddressException ex) {
                throw new AlertException(String.format("%s is not a valid email address. %s", destination, ex.getMessage()));
            }
            return List.of(destination);
        }
        return List.of();
    }

}
