/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.channel.email.descriptor;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.EmailGroupChannel;
import com.synopsys.integration.alert.channel.email.EmailProperties;
import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.descriptor.config.context.DescriptorActionApi;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.web.model.FieldModel;
import com.synopsys.integration.alert.web.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class EmailGlobalDescriptorActionApi extends DescriptorActionApi {
    public static final String NOT_AN_INTEGER = "Not an Integer.";
    private final EmailGroupChannel emailGroupChannel;

    @Autowired
    public EmailGlobalDescriptorActionApi(final EmailGroupChannel emailGroupChannel) {
        this.emailGroupChannel = emailGroupChannel;
    }

    // TODO Global email config doesn't validate properly or give any indication that saving was successful
    @Override
    public void validateConfig(final FieldModel fieldModel, final Map<String, String> fieldErrors) {
        final String port = fieldModel.getField(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey()).flatMap(field -> field.getValue()).orElse(null);
        final String connectionTimeout = fieldModel.getField(EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY.getPropertyKey()).flatMap(field -> field.getValue()).orElse(null);
        final String timeout = fieldModel.getField(EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY.getPropertyKey()).flatMap(field -> field.getValue()).orElse(null);

        if (StringUtils.isNotBlank(port) && !StringUtils.isNumeric(port)) {
            fieldErrors.put("mailSmtpPort", NOT_AN_INTEGER);
        }
        if (StringUtils.isNotBlank(connectionTimeout) && !StringUtils.isNumeric(connectionTimeout)) {
            fieldErrors.put("mailSmtpConnectionTimeout", NOT_AN_INTEGER);
        }
        if (StringUtils.isNotBlank(timeout) && !StringUtils.isNumeric(timeout)) {
            fieldErrors.put("mailSmtpTimeout", NOT_AN_INTEGER);
        }
    }

    @Override
    public void testConfig(final TestConfigModel testConfig) throws IntegrationException {
        Set<String> emailAddresses = null;
        final String testEmailAddress = testConfig.getDestination().orElse(null);
        if (StringUtils.isNotBlank(testEmailAddress)) {
            try {
                final InternetAddress emailAddr = new InternetAddress(testEmailAddress);
                emailAddr.validate();
            } catch (final AddressException ex) {
                throw new AlertException(String.format("%s is not a valid email address. %s", testEmailAddress, ex.getMessage()));
            }
            emailAddresses = Collections.singleton(testEmailAddress);
        }
        final FieldAccessor fieldAccessor = testConfig.getFieldModel().convertToFieldAccessor();
        final EmailProperties emailProperties = new EmailProperties(fieldAccessor);
        final AggregateMessageContent messageContent = new AggregateMessageContent("Message Content", "Test from Alert", Collections.emptyList());
        emailGroupChannel.sendMessage(emailProperties, emailAddresses, "Test from Alert", "Global Configuration", "", messageContent, "N/A");
    }

}
