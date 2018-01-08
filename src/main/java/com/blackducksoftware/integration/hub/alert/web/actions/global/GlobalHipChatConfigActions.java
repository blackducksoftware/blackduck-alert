/**
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.alert.web.actions.global;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHipChatRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.ConfigActions;
import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalHipChatConfigRestModel;

@Component
public class GlobalHipChatConfigActions extends ConfigActions<GlobalHipChatConfigEntity, GlobalHipChatConfigRestModel, GlobalHipChatRepositoryWrapper> {
    @Autowired
    public GlobalHipChatConfigActions(final GlobalHipChatRepositoryWrapper hipChatRepository, final ObjectTransformer objectTransformer) {
        super(GlobalHipChatConfigEntity.class, GlobalHipChatConfigRestModel.class, hipChatRepository, objectTransformer);
    }

    @Override
    public String validateConfig(final GlobalHipChatConfigRestModel restModel) throws AlertFieldException {
        return "Valid";
    }

    @Override
    public String channelTestConfig(final GlobalHipChatConfigRestModel restModel) throws IntegrationException {
        // TODO decide how to test the API key
        return "Not implemented.";
    }

}
