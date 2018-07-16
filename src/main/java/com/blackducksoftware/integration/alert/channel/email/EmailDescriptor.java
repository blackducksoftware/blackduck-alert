/**
 * blackduck-alert
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
package com.blackducksoftware.integration.alert.channel.email;

import java.lang.reflect.Field;
import java.util.Map;

import javax.jms.MessageListener;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.channel.email.model.EmailGlobalConfigEntity;
import com.blackducksoftware.integration.alert.channel.email.model.EmailGlobalConfigRestModel;
import com.blackducksoftware.integration.alert.channel.email.model.EmailGroupDistributionConfigEntity;
import com.blackducksoftware.integration.alert.channel.email.model.EmailGroupDistributionRestModel;
import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.descriptor.ChannelDescriptor;
import com.blackducksoftware.integration.alert.event.ChannelEvent;
import com.blackducksoftware.integration.alert.web.model.CommonDistributionConfigRestModel;
import com.blackducksoftware.integration.alert.web.model.ConfigRestModel;
import com.blackducksoftware.integration.exception.IntegrationException;

@Component
public class EmailDescriptor extends ChannelDescriptor {
    private final EmailGroupChannel emailGroupChannel;

    @Autowired
    public EmailDescriptor(final EmailGroupChannel emailGroupChannel, final EmailGlobalContentConverter emailGlobalContentConverter, final EmailGlobalRepositoryAccessor emailGlobalRepositoryAccessor,
            final EmailDistributionContentConverter emailDistributionContentConverter, final EmailDistributionRepositoryAccessor emailDistributionRepositoryAccessor) {
        super(EmailGroupChannel.COMPONENT_NAME, EmailGroupChannel.COMPONENT_NAME, emailGlobalContentConverter, emailGlobalRepositoryAccessor, emailDistributionContentConverter, emailDistributionRepositoryAccessor);
        this.emailGroupChannel = emailGroupChannel;
    }

    @Override
    public void validateDistributionConfig(final CommonDistributionConfigRestModel restModel, final Map<String, String> fieldErrors) {
        if (restModel instanceof EmailGroupDistributionRestModel) {
            final EmailGroupDistributionRestModel emailRestModel = (EmailGroupDistributionRestModel) restModel;
            if (StringUtils.isBlank(emailRestModel.getGroupName())) {
                fieldErrors.put("groupName", "A group must be specified.");
            }
        }
    }

    @Override
    public void testDistributionConfig(final CommonDistributionConfigRestModel restModel, final ChannelEvent event) throws IntegrationException {
        final EmailGroupDistributionConfigEntity emailEntity = (EmailGroupDistributionConfigEntity) super.getDistributionContentConverter().populateDatabaseEntityFromRestModel(restModel);
        emailGroupChannel.sendAuditedMessage(event, emailEntity);
    }

    @Override
    public MessageListener getChannelListener() {
        return emailGroupChannel;
    }

    @Override
    public void validateGlobalConfig(final ConfigRestModel restModel, final Map<String, String> fieldErrors) {
        if (restModel instanceof EmailGlobalConfigRestModel) {
            final EmailGlobalConfigRestModel emailRestModel = (EmailGlobalConfigRestModel) restModel;

            if (StringUtils.isNotBlank(emailRestModel.getMailSmtpPort()) && !StringUtils.isNumeric(emailRestModel.getMailSmtpPort())) {
                fieldErrors.put("mailSmtpPort", "Not an Integer.");
            }
            if (StringUtils.isNotBlank(emailRestModel.getMailSmtpConnectionTimeout()) && !StringUtils.isNumeric(emailRestModel.getMailSmtpConnectionTimeout())) {
                fieldErrors.put("mailSmtpConnectionTimeout", "Not an Integer.");
            }
            if (StringUtils.isNotBlank(emailRestModel.getMailSmtpTimeout()) && !StringUtils.isNumeric(emailRestModel.getMailSmtpTimeout())) {
                fieldErrors.put("mailSmtpTimeout", "Not an Integer.");
            }
        }
    }

    @Override
    public void testGlobalConfig(final DatabaseEntity entity) throws IntegrationException {
        throw new IntegrationException("Error, can't test global email configuration");
    }

    @Override
    public Field[] getGlobalEntityFields() {
        return EmailGlobalConfigEntity.class.getDeclaredFields();
    }

    @Override
    public ConfigRestModel getGlobalRestModelObject() {
        return new EmailGlobalConfigRestModel();
    }

}
