/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.web.actions;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.channel.email.EmailChannel;
import com.blackducksoftware.integration.hub.alert.datasource.entity.EmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.EmailRepository;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.EmailConfigRestModel;

@Component
public class EmailConfigActions extends ConfigActions<EmailConfigEntity, EmailConfigRestModel> {
    private final EmailChannel emailChannel;

    @Autowired
    public EmailConfigActions(final EmailRepository emailRepository, final ObjectTransformer objectTransformer, final EmailChannel emailChannel) {
        super(EmailConfigEntity.class, EmailConfigRestModel.class, emailRepository, objectTransformer);
        this.emailChannel = emailChannel;
    }

    @Override
    public EmailConfigEntity updateNewConfigWithSavedConfig(final EmailConfigEntity newConfig, final EmailConfigEntity savedConfig) {
        final Long id = newConfig.getId();
        final String mailSmtpHost = newConfig.getMailSmtpHost();
        final String mailSmtpUser = newConfig.getMailSmtpUser();
        String mailSmtpPassword = "";
        if (StringUtils.isBlank(newConfig.getMailSmtpPassword())) {
            mailSmtpPassword = savedConfig.getMailSmtpPassword();
        } else {
            mailSmtpPassword = newConfig.getMailSmtpPassword();
        }
        final Integer mailSmtpPort = newConfig.getMailSmtpPort();
        final Integer mailSmtpConnectionTimeout = newConfig.getMailSmtpConnectionTimeout();
        final Integer mailSmtpTimeout = newConfig.getMailSmtpTimeout();
        final String mailSmtpFrom = newConfig.getMailSmtpFrom();
        final String mailSmtpLocalhost = newConfig.getMailSmtpLocalhost();
        final Boolean mailSmtpEhlo = newConfig.getMailSmtpEhlo();
        final Boolean mailSmtpAuth = newConfig.getMailSmtpAuth();
        final String mailSmtpDnsNotify = newConfig.getMailSmtpDnsNotify();
        final String mailSmtpDnsRet = newConfig.getMailSmtpDnsRet();
        final Boolean mailSmtpAllow8bitmime = newConfig.getMailSmtpAllow8bitmime();
        final Boolean mailSmtpSendPartial = newConfig.getMailSmtpSendPartial();
        final String emailTemplateDirectory = newConfig.getEmailTemplateDirectory();
        final String emailTemplateLogoImage = newConfig.getEmailTemplateLogoImage();
        final String emailSubjectLine = newConfig.getEmailSubjectLine();

        final EmailConfigEntity emailConfigEntity = new EmailConfigEntity(mailSmtpHost, mailSmtpUser, mailSmtpPassword, mailSmtpPort, mailSmtpConnectionTimeout, mailSmtpTimeout, mailSmtpFrom, mailSmtpLocalhost, mailSmtpEhlo, mailSmtpAuth,
                mailSmtpDnsNotify, mailSmtpDnsRet, mailSmtpAllow8bitmime, mailSmtpSendPartial, emailTemplateDirectory, emailTemplateLogoImage, emailSubjectLine);
        emailConfigEntity.setId(id);
        return emailConfigEntity;
    }

    @Override
    public EmailConfigRestModel maskRestModel(final EmailConfigRestModel restModel) {
        final EmailConfigRestModel maskedRestModel = new EmailConfigRestModel(restModel.getId(), restModel.getMailSmtpHost(), restModel.getMailSmtpUser(), "", restModel.getMailSmtpPort(), restModel.getMailSmtpConnectionTimeout(),
                restModel.getMailSmtpTimeout(), restModel.getMailSmtpFrom(), restModel.getMailSmtpLocalhost(), restModel.getMailSmtpEhlo(), restModel.getMailSmtpAuth(), restModel.getMailSmtpDnsNotify(), restModel.getMailSmtpDnsRet(),
                restModel.getMailSmtpAllow8bitmime(), restModel.getMailSmtpSendPartial(), restModel.getEmailTemplateDirectory(), restModel.getEmailTemplateLogoImage(), restModel.getEmailSubjectLine());
        return maskedRestModel;
    }

    @Override
    public String validateConfig(final EmailConfigRestModel restModel) throws AlertFieldException {
        final Map<String, String> fieldErrors = new HashMap<>();
        if (StringUtils.isNotBlank(restModel.getMailSmtpPort()) && !StringUtils.isNumeric(restModel.getMailSmtpPort())) {
            fieldErrors.put("mailSmtpPort", "Not an Integer.");
        }
        if (StringUtils.isNotBlank(restModel.getMailSmtpConnectionTimeout()) && !StringUtils.isNumeric(restModel.getMailSmtpConnectionTimeout())) {
            fieldErrors.put("mailSmtpConnectionTimeout", "Not an Integer.");
        }
        if (StringUtils.isNotBlank(restModel.getMailSmtpTimeout()) && !StringUtils.isNumeric(restModel.getMailSmtpTimeout())) {
            fieldErrors.put("mailSmtpTimeout", "Not an Integer.");
        }

        if (StringUtils.isNotBlank(restModel.getMailSmtpEhlo()) && !isBoolean(restModel.getMailSmtpEhlo())) {
            fieldErrors.put("mailSmtpEhlo", "Not an Boolean.");
        }
        if (StringUtils.isNotBlank(restModel.getMailSmtpAuth()) && !isBoolean(restModel.getMailSmtpAuth())) {
            fieldErrors.put("mailSmtpAuth", "Not an Boolean.");
        }
        if (StringUtils.isNotBlank(restModel.getMailSmtpAllow8bitmime()) && !isBoolean(restModel.getMailSmtpAllow8bitmime())) {
            fieldErrors.put("mailSmtpAllow8bitmime", "Not an Boolean.");
        }
        if (StringUtils.isNotBlank(restModel.getMailSmtpSendPartial()) && !isBoolean(restModel.getMailSmtpSendPartial())) {
            fieldErrors.put("mailSmtpSendPartial", "Not an Boolean.");
        }
        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return "Valid";
    }

    @Override
    public String testConfig(final EmailConfigRestModel restModel) throws IntegrationException {
        EmailConfigRestModel testModel = restModel;
        if (restModel != null && StringUtils.isNotBlank(restModel.getId()) && StringUtils.isBlank(restModel.getMailSmtpPassword())) {
            final Long longId = objectTransformer.stringToLong(restModel.getId());
            final EmailConfigEntity savedConfig = repository.findOne(longId);

            final String id = restModel.getId();
            final String mailSmtpHost = restModel.getMailSmtpHost();
            final String mailSmtpUser = restModel.getMailSmtpUser();
            String mailSmtpPassword = "";
            if (StringUtils.isBlank(restModel.getMailSmtpPassword())) {
                mailSmtpPassword = savedConfig.getMailSmtpPassword();
            } else {
                mailSmtpPassword = restModel.getMailSmtpPassword();
            }
            final String mailSmtpPort = restModel.getMailSmtpPort();
            final String mailSmtpConnectionTimeout = restModel.getMailSmtpConnectionTimeout();
            final String mailSmtpTimeout = restModel.getMailSmtpTimeout();
            final String mailSmtpFrom = restModel.getMailSmtpFrom();
            final String mailSmtpLocalhost = restModel.getMailSmtpLocalhost();
            final String mailSmtpEhlo = restModel.getMailSmtpEhlo();
            final String mailSmtpAuth = restModel.getMailSmtpAuth();
            final String mailSmtpDnsNotify = restModel.getMailSmtpDnsNotify();
            final String mailSmtpDnsRet = restModel.getMailSmtpDnsRet();
            final String mailSmtpAllow8bitmime = restModel.getMailSmtpAllow8bitmime();
            final String mailSmtpSendPartial = restModel.getMailSmtpSendPartial();
            final String emailTemplateDirectory = restModel.getEmailTemplateDirectory();
            final String emailTemplateLogoImage = restModel.getEmailTemplateLogoImage();
            final String emailSubjectLine = restModel.getEmailSubjectLine();

            testModel = new EmailConfigRestModel(id, mailSmtpHost, mailSmtpUser, mailSmtpPassword, mailSmtpPort, mailSmtpConnectionTimeout, mailSmtpTimeout, mailSmtpFrom, mailSmtpLocalhost, mailSmtpEhlo, mailSmtpAuth, mailSmtpDnsNotify,
                    mailSmtpDnsRet, mailSmtpAllow8bitmime, mailSmtpSendPartial, emailTemplateDirectory, emailTemplateLogoImage, emailSubjectLine);
        }
        return emailChannel.testMessage(objectTransformer.configRestModelToDatabaseEntity(testModel, EmailConfigEntity.class));
    }

}
