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
package com.synopsys.integration.alert.component.settings;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.EmailChannel;
import com.synopsys.integration.alert.channel.email.EmailMessagingService;
import com.synopsys.integration.alert.channel.email.EmailProperties;
import com.synopsys.integration.alert.channel.email.template.EmailTarget;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.UserModel;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.database.api.UserAccessor;

@Component
public class PasswordResetService {
    private static final int TEMP_PASSWORD_LENGTH = 50;
    private static final String TEMPLATE_NAME = "password_reset.ftl";
    private static final String SUBJECT_LINE = "Alert - Password Reset";

    private final AlertProperties alertProperties;
    private final UserAccessor userAccessor;
    private final BaseConfigurationAccessor configurationAccessor;

    @Autowired
    public PasswordResetService(final AlertProperties alertProperties, final UserAccessor userAccessor, final BaseConfigurationAccessor configurationAccessor) {
        this.alertProperties = alertProperties;
        this.userAccessor = userAccessor;
        this.configurationAccessor = configurationAccessor;
    }

    public void resetPassword(final String username) throws AlertException {
        final UserModel userModel = userAccessor.getUser(username)
                                        .orElseThrow(() -> new AlertDatabaseConstraintException("No user exists for the username: " + username));
        if (StringUtils.isBlank(userModel.getEmailAddress())) {
            throw new AlertException("No email address configured for user: " + username);
        }
        final ConfigurationModel emailConfig = configurationAccessor.getConfigurationByDescriptorNameAndContext(EmailChannel.COMPONENT_NAME, ConfigContextEnum.GLOBAL)
                                                   .stream()
                                                   .findFirst()
                                                   .orElseThrow(() -> new AlertException("No global email configuration found"));
        final FieldAccessor fieldAccessor = new FieldAccessor(emailConfig.getCopyOfKeyToFieldMap());
        final EmailProperties emailProperties = new EmailProperties(fieldAccessor);

        final String tempPassword = RandomStringUtils.randomAlphanumeric(TEMP_PASSWORD_LENGTH);
        final Map<String, Object> templateFields = new HashMap<>();
        templateFields.put(EmailPropertyKeys.TEMPLATE_KEY_SUBJECT_LINE.getPropertyKey(), SUBJECT_LINE);
        templateFields.put("tempPassword", tempPassword);
        handleSendAndUpdateDatabase(emailProperties, templateFields, userModel.getEmailAddress(), username, tempPassword);
    }

    private void handleSendAndUpdateDatabase(final EmailProperties emailProperties, final Map<String, Object> templateFields, final String emailAddress, final String username, final String tempPassword) throws AlertException {
        try {
            final String imagesDirectory = alertProperties.getAlertImagesDir();
            final String imageDirectoryPath;
            if (StringUtils.isNotBlank(imagesDirectory)) {
                imageDirectoryPath = imagesDirectory + "/synopsys.png";
            } else {
                imageDirectoryPath = System.getProperties().getProperty("user.dir") + "/src/main/resources/email/images/synopsys.png";
            }

            final Map<String, String> contentIdsToFilePaths = new HashMap<>();
            final EmailMessagingService emailService = new EmailMessagingService(alertProperties.getAlertTemplatesDir(), emailProperties);
            emailService.addTemplateImage(templateFields, contentIdsToFilePaths, EmailPropertyKeys.EMAIL_LOGO_IMAGE.getPropertyKey(), imageDirectoryPath);

            final EmailTarget passwordResetEmail = new EmailTarget(emailAddress, TEMPLATE_NAME, templateFields, contentIdsToFilePaths);
            emailService.sendEmailMessage(passwordResetEmail);
            // Only change the password if there isn't an issue with sending the email
            userAccessor.changeUserPassword(username, tempPassword);
        } catch (final IOException ioException) {
            throw new AlertException("Problem sending password reset email.", ioException);
        } catch (final Exception genericException) {
            throw new AlertException("Problem sending password reset email. " + StringUtils.defaultIfBlank(genericException.getMessage(), ""), genericException);
        }
    }
}
