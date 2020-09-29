/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.email.actions;

import java.util.List;
import java.util.Set;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.EmailChannel;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.channel.email.template.EmailAttachmentFormat;
import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.email.EmailProperties;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.provider.state.ProviderProperties;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class EmailGlobalTestAction extends TestAction {
    private final EmailChannel emailChannel;

    @Autowired
    public EmailGlobalTestAction(EmailChannel emailChannel) {
        this.emailChannel = emailChannel;
    }

    @Override
    public MessageResult testConfig(String configId, FieldModel fieldModel, FieldUtility registeredFieldValues) throws IntegrationException {
        Set<String> emailAddresses = Set.of();
        String destination = fieldModel.getFieldValue(TestAction.KEY_DESTINATION_NAME).orElse("");
        if (StringUtils.isNotBlank(destination)) {
            try {
                InternetAddress emailAddr = new InternetAddress(destination);
                emailAddr.validate();
            } catch (AddressException ex) {
                throw new AlertException(String.format("%s is not a valid email address. %s", destination, ex.getMessage()));
            }
            emailAddresses = Set.of(destination);
        }
        EmailProperties emailProperties = new EmailProperties(registeredFieldValues);
        ComponentItem.Builder componentBuilder = new ComponentItem.Builder()
                                                     .applyCategory("Test")
                                                     .applyOperation(ItemOperation.ADD)
                                                     .applyComponentData("Component", "Global Email Configuration")
                                                     .applyCategoryItem("Message", "This is a test message from Alert.")
                                                     .applyNotificationId(1L);

        ProviderMessageContent.Builder builder = new ProviderMessageContent.Builder()
                                                     .applyProvider("Test Provider", ProviderProperties.UNKNOWN_CONFIG_ID, "Test Provider Config")
                                                     .applyTopic("Message Content", "Test from Alert")
                                                     .applyAllComponentItems(List.of(componentBuilder.build()));

        ProviderMessageContent messageContent = builder.build();

        EmailAttachmentFormat attachmentFormat = registeredFieldValues.getString(EmailDescriptor.KEY_EMAIL_ATTACHMENT_FORMAT)
                                                     .map(EmailAttachmentFormat::getValueSafely)
                                                     .orElse(EmailAttachmentFormat.NONE);
        emailChannel.sendMessage(emailProperties, emailAddresses, "Test from Alert", "", attachmentFormat, MessageContentGroup.singleton(messageContent));
        return new MessageResult("Message sent");
    }

}
