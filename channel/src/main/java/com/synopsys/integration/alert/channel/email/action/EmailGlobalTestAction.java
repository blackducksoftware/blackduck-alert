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

import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.channel.email.distribution.EmailChannelV2;
import com.synopsys.integration.alert.channel.email.template.EmailAttachmentFormat;
import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;

@Component
public class EmailGlobalTestAction extends TestAction {
    private final EmailChannelV2 emailChannel;

    @Autowired
    public EmailGlobalTestAction(EmailChannelV2 emailChannel) {
        this.emailChannel = emailChannel;
    }

    @Override
    public MessageResult testConfig(String configId, FieldModel fieldModel, FieldUtility registeredFieldValues) throws AlertException {
        List<String> emailAddresses = extractAndValidateDestination(fieldModel);

        EmailAttachmentFormat attachmentFormat = registeredFieldValues.getString(EmailDescriptor.KEY_EMAIL_ATTACHMENT_FORMAT)
                                                     .map(EmailAttachmentFormat::getValueSafely)
                                                     .orElse(EmailAttachmentFormat.NONE);

        EmailJobDetailsModel distributionDetails = new EmailJobDetailsModel(null, "Subject Line", false, true, attachmentFormat.name(), emailAddresses);

        LinkableItem provider = new LinkableItem("Test Provider", "Test Provider Config");
        ProviderDetails providerDetails = new ProviderDetails(0L, provider);

        SimpleMessage testMessage = SimpleMessage.original(providerDetails, "Test from Alert", "This is a test message from Alert.", List.of());
        ProviderMessageHolder providerMessageHolder = new ProviderMessageHolder(List.of(), List.of(testMessage));

        return emailChannel.distributeMessages(distributionDetails, providerMessageHolder);
        // TODO does this result string matter?
        //  return new MessageResult("Message sent");
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
