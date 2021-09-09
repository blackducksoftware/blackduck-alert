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
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

@Component
public class EmailGlobalTestAction {
    private static final String TEST_SUBJECT_LINE = "Email Global Configuration Test";
    private static final String TEST_MESSAGE_CONTENT = "This is a test message from Alert to confirm your Global Email Configuration is valid.";

    private final EmailChannelMessageSender emailChannelMessageSender;

    @Autowired
    public EmailGlobalTestAction(EmailChannelMessageSender emailChannelMessageSender) {
        this.emailChannelMessageSender = emailChannelMessageSender;
    }

    public MessageResult testConfig(String testAddress, EmailGlobalConfigModel emailGlobalConfigModel) throws AlertException {
        List<String> emailAddresses = validateAndWrapDestinationAsList(testAddress);
        EmailJobDetailsModel distributionDetails = new EmailJobDetailsModel(null, TEST_SUBJECT_LINE, false, true, EmailAttachmentFormat.NONE.name(), emailAddresses);

        EmailChannelMessageModel testMessage = EmailChannelMessageModel.simple(TEST_SUBJECT_LINE, TEST_MESSAGE_CONTENT, "", "");

        return emailChannelMessageSender.sendMessages(emailGlobalConfigModel, distributionDetails, List.of(testMessage));
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
