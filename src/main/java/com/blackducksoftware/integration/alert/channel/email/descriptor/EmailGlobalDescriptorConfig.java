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
package com.blackducksoftware.integration.alert.channel.email.descriptor;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.channel.email.EmailGroupChannel;
import com.blackducksoftware.integration.alert.common.descriptor.config.DescriptorConfig;
import com.blackducksoftware.integration.alert.common.descriptor.config.UIComponent;
import com.blackducksoftware.integration.alert.database.channel.email.EmailGlobalConfigEntity;
import com.blackducksoftware.integration.alert.database.channel.email.EmailGlobalRepositoryAccessor;
import com.blackducksoftware.integration.alert.database.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.web.channel.model.EmailGlobalConfig;
import com.blackducksoftware.integration.alert.web.model.Config;
import com.blackducksoftware.integration.exception.IntegrationException;

@Component
public class EmailGlobalDescriptorConfig extends DescriptorConfig {
    public static final String NOT_AN_INTEGER = "Not an Integer.";

    private final EmailGroupChannel emailGroupChannel;

    @Autowired
    public EmailGlobalDescriptorConfig(final EmailGlobalContentConverter databaseContentConverter, final EmailGlobalRepositoryAccessor repositoryAccessor, final EmailGlobalStartupComponent startupComponent,
            final EmailGroupChannel emailGroupChannel) {
        super(databaseContentConverter, repositoryAccessor);
        this.emailGroupChannel = emailGroupChannel;
        setStartupComponent(startupComponent);
    }

    @Override
    public void validateConfig(final Config restModel, final Map<String, String> fieldErrors) {
        final EmailGlobalConfig emailRestModel = (EmailGlobalConfig) restModel;

        if (StringUtils.isNotBlank(emailRestModel.getMailSmtpPort()) && !StringUtils.isNumeric(emailRestModel.getMailSmtpPort())) {
            fieldErrors.put("mailSmtpPort", NOT_AN_INTEGER);
        }
        if (StringUtils.isNotBlank(emailRestModel.getMailSmtpConnectionTimeout()) && !StringUtils.isNumeric(emailRestModel.getMailSmtpConnectionTimeout())) {
            fieldErrors.put("mailSmtpConnectionTimeout", NOT_AN_INTEGER);
        }
        if (StringUtils.isNotBlank(emailRestModel.getMailSmtpTimeout()) && !StringUtils.isNumeric(emailRestModel.getMailSmtpTimeout())) {
            fieldErrors.put("mailSmtpTimeout", NOT_AN_INTEGER);
        }
    }

    @Override
    public void testConfig(final DatabaseEntity entity) throws IntegrationException {
        final EmailGlobalConfigEntity emailEntity = (EmailGlobalConfigEntity) entity;
        emailGroupChannel.testGlobalConfig(emailEntity);
    }

    @Override
    public UIComponent getUiComponent() {
        return new UIComponent("Email", "envelope", "EmailConfiguration");
    }

}
