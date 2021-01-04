/**
 * component
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.component.authentication.web;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.channel.template.FreemarkerTemplatingService;
import com.synopsys.integration.alert.common.email.EmailMessagingService;
import com.synopsys.integration.alert.common.email.EmailProperties;
import com.synopsys.integration.alert.common.email.EmailTarget;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

@Component
public class PasswordResetService {
    private static final int TEMP_PASSWORD_LENGTH = 50;
    private static final String TEMPLATE_NAME = "password_reset.ftl";
    private static final String SUBJECT_LINE = "Alert - Password Reset";

    private final AlertProperties alertProperties;
    private final UserAccessor userAccessor;
    private final ConfigurationAccessor configurationAccessor;
    private final FreemarkerTemplatingService freemarkerTemplatingService;

    @Autowired
    public PasswordResetService(AlertProperties alertProperties, UserAccessor userAccessor, ConfigurationAccessor configurationAccessor, FreemarkerTemplatingService freemarkerTemplatingService) {
        this.alertProperties = alertProperties;
        this.userAccessor = userAccessor;
        this.configurationAccessor = configurationAccessor;
        this.freemarkerTemplatingService = freemarkerTemplatingService;
    }

    public void resetPassword(String username) throws AlertException {
        UserModel userModel = userAccessor.getUser(username)
                                  .orElseThrow(() -> new AlertConfigurationException("No user exists for the username: " + username));
        if (StringUtils.isBlank(userModel.getEmailAddress())) {
            throw new AlertConfigurationException("No email address configured for user: " + username);
        }
        ConfigurationModel emailConfig = configurationAccessor.getConfigurationsByDescriptorKeyAndContext(ChannelKeys.EMAIL, ConfigContextEnum.GLOBAL)
                                             .stream()
                                             .findFirst()
                                             .orElseThrow(() -> new AlertConfigurationException("No global email configuration found"));
        FieldUtility fieldUtility = new FieldUtility(emailConfig.getCopyOfKeyToFieldMap());
        EmailProperties emailProperties = new EmailProperties(fieldUtility);
        String alertServerUrl = alertProperties.getServerUrl().orElse(null);
        String tempPassword = RandomStringUtils.randomAlphanumeric(TEMP_PASSWORD_LENGTH);
        Map<String, Object> templateFields = new HashMap<>();
        templateFields.put(EmailPropertyKeys.TEMPLATE_KEY_SUBJECT_LINE.getPropertyKey(), SUBJECT_LINE);
        templateFields.put("tempPassword", tempPassword);
        templateFields.put(FreemarkerTemplatingService.KEY_ALERT_SERVER_URL, alertServerUrl);
        handleSendAndUpdateDatabase(emailProperties, templateFields, userModel.getEmailAddress(), username, tempPassword);
    }

    private void handleSendAndUpdateDatabase(EmailProperties emailProperties, Map<String, Object> templateFields, String emailAddress, String username, String tempPassword) throws AlertException {
        try {
            String alertLogo = alertProperties.getAlertLogo();

            Map<String, String> contentIdsToFilePaths = new HashMap<>();
            EmailMessagingService emailService = new EmailMessagingService(emailProperties, freemarkerTemplatingService);
            emailService.addTemplateImage(templateFields, contentIdsToFilePaths, EmailPropertyKeys.EMAIL_LOGO_IMAGE.getPropertyKey(), alertLogo);

            EmailTarget passwordResetEmail = new EmailTarget(emailAddress, TEMPLATE_NAME, templateFields, contentIdsToFilePaths);
            emailService.sendEmailMessage(passwordResetEmail);
            // Only change the password if there isn't an issue with sending the email
            userAccessor.changeUserPassword(username, tempPassword);
        } catch (Exception genericException) {
            throw new AlertException("Problem sending password reset email. " + StringUtils.defaultIfBlank(genericException.getMessage(), ""), genericException);
        }
    }

}
