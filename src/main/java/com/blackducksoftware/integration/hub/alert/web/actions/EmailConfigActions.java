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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.channel.email.EmailChannel;
import com.blackducksoftware.integration.hub.alert.datasource.entity.EmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.repository.EmailRepository;
import com.blackducksoftware.integration.hub.alert.datasource.repository.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.EmailConfigRestModel;
import com.google.gson.Gson;

@Component
public class EmailConfigActions extends ConfigActions<EmailConfigEntity, EmailConfigRestModel> {
    private final GlobalProperties globalProperties;
    private final Gson gson;

    @Autowired
    public EmailConfigActions(final EmailRepository emailRepository, final ObjectTransformer objectTransformer, final GlobalProperties globalProperties, final Gson gson) {
        super(EmailConfigEntity.class, EmailConfigRestModel.class, emailRepository, objectTransformer);
        this.globalProperties = globalProperties;
        this.gson = gson;
    }

    @Override
    public String validateConfig(final EmailConfigRestModel restModel) throws AlertFieldException {
        final Map<String, String> fieldErrors = new HashMap<>();

        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return "";
    }

    @Override
    public String testConfig(final EmailConfigRestModel restModel) throws IntegrationException {
        final EmailChannel channel = new EmailChannel(globalProperties, gson, (EmailRepository) repository);
        return channel.testMessage(objectTransformer.configRestModelToDatabaseEntity(restModel, EmailConfigEntity.class));
    }

}
