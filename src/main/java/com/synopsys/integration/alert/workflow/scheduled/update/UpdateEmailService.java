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
package com.synopsys.integration.alert.workflow.scheduled.update;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.channel.template.FreemarkerTemplatingService;
import com.synopsys.integration.alert.common.email.EmailMessagingService;
import com.synopsys.integration.alert.common.email.EmailProperties;
import com.synopsys.integration.alert.common.email.EmailTarget;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.accessor.SettingsKeyAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.SettingsKeyModel;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.alert.workflow.scheduled.update.model.UpdateModel;

@Component
public class UpdateEmailService {
    public static final String SETTINGS_KEY_VERSION_FOR_UPDATE_EMAIL = "update.email.sent.for.version";

    private static final String TEMPLATE_NAME = "update_available.ftl";
    private static final String SUBJECT_LINE = "A new version of Alert is available";

    private final Logger logger = LoggerFactory.getLogger(UpdateEmailService.class);
    private final AlertProperties alertProperties;
    private final SettingsKeyAccessor settingsKeyAccessor;
    private final UserAccessor userAccessor;
    private final ConfigurationAccessor configurationAccessor;
    private final FreemarkerTemplatingService freemarkerTemplatingService;

    @Autowired
    public UpdateEmailService(AlertProperties alertProperties, SettingsKeyAccessor settingsKeyAccessor, UserAccessor userAccessor, ConfigurationAccessor configurationAccessor,
        FreemarkerTemplatingService freemarkerTemplatingService) {
        this.alertProperties = alertProperties;
        this.settingsKeyAccessor = settingsKeyAccessor;
        this.userAccessor = userAccessor;
        this.configurationAccessor = configurationAccessor;
        this.freemarkerTemplatingService = freemarkerTemplatingService;
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
                ConfigurationModel emailConfig = configurationAccessor.getConfigurationsByDescriptorKeyAndContext(ChannelKey.EMAIL, ConfigContextEnum.GLOBAL)
                                                     .stream()
                                                     .findFirst()
                                                     .orElseThrow(() -> new AlertException("No global email configuration found"));
                FieldUtility fieldUtility = new FieldUtility(emailConfig.getCopyOfKeyToFieldMap());
                EmailProperties emailProperties = new EmailProperties(fieldUtility);

                String alertServerUrl = alertProperties.getServerUrl().orElse(null);
                Map<String, Object> templateFields = new HashMap<>();
                templateFields.put(EmailPropertyKeys.TEMPLATE_KEY_SUBJECT_LINE.getPropertyKey(), SUBJECT_LINE);
                templateFields.put("newVersionName", updateVersion);
                templateFields.put("repositoryUrl", updateModel.getRepositoryUrl());
                templateFields.put(FreemarkerTemplatingService.KEY_ALERT_SERVER_URL, alertServerUrl);
                handleSendAndUpdateDatabase(emailProperties, templateFields, optionalEmailAddress.get());

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

    private void handleSendAndUpdateDatabase(EmailProperties emailProperties, Map<String, Object> templateFields, String emailAddress) throws AlertException {
        try {
            String alertLogo = alertProperties.getAlertLogo();

            Map<String, String> contentIdsToFilePaths = new HashMap<>();
            EmailMessagingService emailService = new EmailMessagingService(emailProperties, freemarkerTemplatingService);
            emailService.addTemplateImage(templateFields, contentIdsToFilePaths, EmailPropertyKeys.EMAIL_LOGO_IMAGE.getPropertyKey(), alertLogo);

            EmailTarget passwordResetEmail = new EmailTarget(emailAddress, TEMPLATE_NAME, templateFields, contentIdsToFilePaths);
            emailService.sendEmailMessage(passwordResetEmail);
        } catch (Exception genericException) {
            throw new AlertException("Problem sending version update email. " + StringUtils.defaultIfBlank(genericException.getMessage(), StringUtils.EMPTY), genericException);
        }
    }

}
