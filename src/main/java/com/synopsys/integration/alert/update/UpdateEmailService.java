/*
 * blackduck-alert
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.update;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.email.database.accessor.EmailGlobalConfigAccessor;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.accessor.SettingsKeyAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.SettingsKeyModel;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.service.email.EmailMessagingService;
import com.synopsys.integration.alert.service.email.EmailTarget;
import com.synopsys.integration.alert.service.email.JavamailPropertiesFactory;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;
import com.synopsys.integration.alert.service.email.template.FreemarkerTemplatingService;
import com.synopsys.integration.alert.update.model.UpdateModel;

@Component
public class UpdateEmailService {
    public static final String SETTINGS_KEY_VERSION_FOR_UPDATE_EMAIL = "update.email.sent.for.version";

    private static final String TEMPLATE_NAME = "update_available.ftl";
    private static final String SUBJECT_LINE = "A new version of Alert is available";

    private final Logger logger = LoggerFactory.getLogger(UpdateEmailService.class);
    private final AlertProperties alertProperties;
    private final SettingsKeyAccessor settingsKeyAccessor;
    private final UserAccessor userAccessor;
    private final EmailGlobalConfigAccessor emailGlobalConfigAccessor;
    private final EmailMessagingService emailMessagingService;
    private final JavamailPropertiesFactory javamailPropertiesFactory;

    @Autowired
    public UpdateEmailService(AlertProperties alertProperties, SettingsKeyAccessor settingsKeyAccessor, UserAccessor userAccessor, EmailGlobalConfigAccessor emailGlobalConfigAccessor, EmailMessagingService emailMessagingService,
        JavamailPropertiesFactory javamailPropertiesFactory) {
        this.alertProperties = alertProperties;
        this.settingsKeyAccessor = settingsKeyAccessor;
        this.userAccessor = userAccessor;
        this.emailGlobalConfigAccessor = emailGlobalConfigAccessor;
        this.emailMessagingService = emailMessagingService;
        this.javamailPropertiesFactory = javamailPropertiesFactory;
    }

    public void sendUpdateEmail(UpdateModel updateModel) {
        String updateVersion = updateModel.getDockerTagVersion();
        if (wasEmailAlreadySentForVersion(updateVersion)) {
            return;
        }

        String username = "sysadmin";
        Optional<String> optionalEmailAddress = userAccessor.getUser(username)
                                                    .map(UserModel::getEmailAddress)
                                                    .filter(StringUtils::isNotBlank);
        if (optionalEmailAddress.isPresent()) {
            try {
                EmailGlobalConfigModel emailServerConfiguration = emailGlobalConfigAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)
                                                                      .orElseThrow(() -> new AlertException("No global email configuration found"));

                String alertServerUrl = alertProperties.getServerURL();
                Map<String, Object> templateFields = new HashMap<>();
                templateFields.put(EmailPropertyKeys.TEMPLATE_KEY_SUBJECT_LINE.getPropertyKey(), SUBJECT_LINE);
                templateFields.put("newVersionName", updateVersion);
                templateFields.put("repositoryUrl", updateModel.getRepositoryUrl());
                templateFields.put(FreemarkerTemplatingService.KEY_ALERT_SERVER_URL, alertServerUrl);

                handleSend(
                    javamailPropertiesFactory.createJavaMailProperties(emailServerConfiguration),
                    emailServerConfiguration.getSmtpFrom().orElse(StringUtils.EMPTY),
                    emailServerConfiguration.getSmtpHost().orElse(StringUtils.EMPTY),
                    emailServerConfiguration.getSmtpPort().orElse(0),
                    emailServerConfiguration.getSmtpAuth().orElse(false),
                    emailServerConfiguration.getSmtpUsername().orElse(StringUtils.EMPTY),
                    emailServerConfiguration.getSmtpPassword().orElse(StringUtils.EMPTY),
                    templateFields,
                    optionalEmailAddress.get()
                );

                settingsKeyAccessor.saveSettingsKey(SETTINGS_KEY_VERSION_FOR_UPDATE_EMAIL, updateVersion);
            } catch (AlertException e) {
                logger.debug("Problem sending version update email.", e);
            }
        } else {
            logger.debug("No email address configured for user: {}", username);
        }
    }

    private boolean wasEmailAlreadySentForVersion(String updateVersion) {
        return settingsKeyAccessor
                   .getSettingsKeyByKey(SETTINGS_KEY_VERSION_FOR_UPDATE_EMAIL)
                   .map(SettingsKeyModel::getValue)
                   .filter(storedValue -> storedValue.equals(updateVersion))
                   .isPresent();
    }

    private void handleSend(Properties javamailProperties, String smtpFrom, String smtpHost, int smtpPort, boolean smtpAuth, String smtpUsername, String smtpPassword, Map<String, Object> templateFields, String emailAddress) throws AlertException {
        try {
            String alertLogo = alertProperties.createSynopsysLogoPath();

            Map<String, String> contentIdsToFilePaths = new HashMap<>();
            emailMessagingService.addTemplateImage(templateFields, contentIdsToFilePaths, EmailPropertyKeys.EMAIL_LOGO_IMAGE.getPropertyKey(), alertLogo);

            EmailTarget passwordResetEmail = new EmailTarget(emailAddress, TEMPLATE_NAME, templateFields, contentIdsToFilePaths);
            emailMessagingService.sendEmailMessage(javamailProperties, smtpFrom, smtpHost, smtpPort, smtpAuth, smtpUsername, smtpPassword, passwordResetEmail);
        } catch (Exception genericException) {
            throw new AlertException("Problem sending version update email. " + StringUtils.defaultIfBlank(genericException.getMessage(), StringUtils.EMPTY), genericException);
        }
    }

}
