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
package com.blackducksoftware.integration.hub.alert.channel.email.controller.global;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.ConfigActions;

@Component
public class GlobalEmailConfigActions extends ConfigActions<GlobalEmailConfigEntity, GlobalEmailConfigRestModel, GlobalEmailRepositoryWrapper> {
    @Autowired
    public GlobalEmailConfigActions(final GlobalEmailRepositoryWrapper emailRepository, final ObjectTransformer objectTransformer) {
        super(GlobalEmailConfigEntity.class, GlobalEmailConfigRestModel.class, emailRepository, objectTransformer);
    }

    @Override
    public String validateConfig(final GlobalEmailConfigRestModel restModel) throws AlertFieldException {
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

        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return "Valid";
    }

    @Override
    public String channelTestConfig(final GlobalEmailConfigRestModel restModel) throws IntegrationException {
        return null;
    }

}
