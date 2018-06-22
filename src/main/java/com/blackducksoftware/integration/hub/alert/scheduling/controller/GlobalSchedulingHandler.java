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
package com.blackducksoftware.integration.hub.alert.scheduling.controller;

import com.blackducksoftware.integration.hub.alert.scheduling.repository.global.GlobalSchedulingConfigEntity;
import com.blackducksoftware.integration.hub.alert.scheduling.repository.global.GlobalSchedulingRepository;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.controller.handler.CommonConfigHandler;

public class GlobalSchedulingHandler extends CommonConfigHandler<GlobalSchedulingConfigEntity, GlobalSchedulingConfigRestModel, GlobalSchedulingRepository> {

    private final GlobalSchedulingConfigActions configActions;

    public GlobalSchedulingHandler(final Class<GlobalSchedulingConfigEntity> databaseEntityClass, final Class<GlobalSchedulingConfigRestModel> configRestModelClass, final GlobalSchedulingConfigActions configActions,
            final ObjectTransformer objectTransformer) {
        super(databaseEntityClass, configRestModelClass, configActions, objectTransformer);
        this.configActions = configActions;
    }
}
