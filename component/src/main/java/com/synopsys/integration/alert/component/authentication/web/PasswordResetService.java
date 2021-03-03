/*
 * component
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

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
                                  .orElseThrow(() -> new AlertDatabaseConstraintException("No user exists for the username: " + username));
        if (StringUtils.isBlank(userModel.getEmailAddress())) {
            throw new AlertException("No email address configured for user: " + username);
        }
        ConfigurationModel emailConfig = configurationAccessor.getConfigurationsByDescriptorKeyAndContext(ChannelKey.EMAIL, ConfigContextEnum.GLOBAL)
                                             .stream()
                                             .findFirst()
                                             .orElseThrow(() -> new AlertException("No global email configuration found"));
        FieldUtility fieldUtility = new FieldUtility(emailConfig.getCopyOfKeyToFieldMap());
        EmailProperties emailProperties = new EmailProperties(fieldUtility);
        String alertServerUrl = alertProperties.getRootURL();
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
