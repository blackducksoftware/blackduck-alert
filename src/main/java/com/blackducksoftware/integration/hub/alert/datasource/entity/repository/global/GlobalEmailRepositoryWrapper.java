/**
 * hub-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.encryption.PasswordDecrypter;
import com.blackducksoftware.integration.encryption.PasswordEncrypter;
import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.alert.datasource.SimpleKeyRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalEmailConfigEntity;

@Component
public class GlobalEmailRepositoryWrapper extends SimpleKeyRepositoryWrapper<GlobalEmailConfigEntity, GlobalEmailRepository> {

    @Autowired
    public GlobalEmailRepositoryWrapper(final GlobalEmailRepository repository) {
        super(repository);
    }

    @Override
    public GlobalEmailConfigEntity encryptSensitiveData(final GlobalEmailConfigEntity entity) throws EncryptionException {
        String mailSmtpPassword = entity.getMailSmtpPassword();
        if (StringUtils.isBlank(mailSmtpPassword)) {
            return entity;
        } else {
            final String mailSmtpHost = entity.getMailSmtpHost();
            final String mailSmtpUser = entity.getMailSmtpUser();
            mailSmtpPassword = PasswordEncrypter.encrypt(entity.getMailSmtpPassword());
            final Integer mailSmtpPort = entity.getMailSmtpPort();
            final Integer mailSmtpConnectionTimeout = entity.getMailSmtpConnectionTimeout();
            final Integer mailSmtpTimeout = entity.getMailSmtpTimeout();
            final String mailSmtpFrom = entity.getMailSmtpFrom();
            final String mailSmtpLocalhost = entity.getMailSmtpLocalhost();
            final Boolean mailSmtpEhlo = entity.getMailSmtpEhlo();
            final Boolean mailSmtpAuth = entity.getMailSmtpAuth();
            final String mailSmtpDnsNotify = entity.getMailSmtpDnsNotify();
            final String mailSmtpDnsRet = entity.getMailSmtpDnsRet();
            final Boolean mailSmtpAllow8bitmime = entity.getMailSmtpAllow8bitmime();
            final Boolean mailSmtpSendPartial = entity.getMailSmtpSendPartial();
            final String emailTemplateDirectory = entity.getEmailTemplateDirectory();
            final String emailTemplateLogoImage = entity.getEmailTemplateLogoImage();
            final String emailSubjectLine = entity.getEmailSubjectLine();
            final GlobalEmailConfigEntity newEntity = new GlobalEmailConfigEntity(mailSmtpHost, mailSmtpUser, mailSmtpPassword, mailSmtpPort, mailSmtpConnectionTimeout, mailSmtpTimeout, mailSmtpFrom, mailSmtpLocalhost, mailSmtpEhlo,
                    mailSmtpAuth, mailSmtpDnsNotify, mailSmtpDnsRet, mailSmtpAllow8bitmime, mailSmtpSendPartial, emailTemplateDirectory, emailTemplateLogoImage, emailSubjectLine);
            newEntity.setId(entity.getId());
            return newEntity;
        }
    }

    @Override
    public GlobalEmailConfigEntity decryptSensitiveData(final GlobalEmailConfigEntity entity) throws EncryptionException {
        String mailSmtpPassword = entity.getMailSmtpPassword();
        if (StringUtils.isBlank(mailSmtpPassword)) {
            return entity;
        } else {
            final String mailSmtpHost = entity.getMailSmtpHost();
            final String mailSmtpUser = entity.getMailSmtpUser();
            mailSmtpPassword = PasswordDecrypter.decrypt(entity.getMailSmtpPassword());
            final Integer mailSmtpPort = entity.getMailSmtpPort();
            final Integer mailSmtpConnectionTimeout = entity.getMailSmtpConnectionTimeout();
            final Integer mailSmtpTimeout = entity.getMailSmtpTimeout();
            final String mailSmtpFrom = entity.getMailSmtpFrom();
            final String mailSmtpLocalhost = entity.getMailSmtpLocalhost();
            final Boolean mailSmtpEhlo = entity.getMailSmtpEhlo();
            final Boolean mailSmtpAuth = entity.getMailSmtpAuth();
            final String mailSmtpDnsNotify = entity.getMailSmtpDnsNotify();
            final String mailSmtpDnsRet = entity.getMailSmtpDnsRet();
            final Boolean mailSmtpAllow8bitmime = entity.getMailSmtpAllow8bitmime();
            final Boolean mailSmtpSendPartial = entity.getMailSmtpSendPartial();
            final String emailTemplateDirectory = entity.getEmailTemplateDirectory();
            final String emailTemplateLogoImage = entity.getEmailTemplateLogoImage();
            final String emailSubjectLine = entity.getEmailSubjectLine();
            final GlobalEmailConfigEntity newEntity = new GlobalEmailConfigEntity(mailSmtpHost, mailSmtpUser, mailSmtpPassword, mailSmtpPort, mailSmtpConnectionTimeout, mailSmtpTimeout, mailSmtpFrom, mailSmtpLocalhost, mailSmtpEhlo,
                    mailSmtpAuth, mailSmtpDnsNotify, mailSmtpDnsRet, mailSmtpAllow8bitmime, mailSmtpSendPartial, emailTemplateDirectory, emailTemplateLogoImage, emailSubjectLine);
            newEntity.setId(entity.getId());
            return newEntity;
        }
    }
}
