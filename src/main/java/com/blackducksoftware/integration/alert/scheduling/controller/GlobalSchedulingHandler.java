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
package com.blackducksoftware.integration.alert.scheduling.controller;

import com.blackducksoftware.integration.alert.ContentConverter;
import com.blackducksoftware.integration.alert.scheduling.model.GlobalSchedulingConfigEntity;
import com.blackducksoftware.integration.alert.scheduling.model.GlobalSchedulingConfigRestModel;
import com.blackducksoftware.integration.alert.scheduling.model.GlobalSchedulingRepository;
import com.blackducksoftware.integration.alert.web.controller.handler.CommonConfigHandler;

public class GlobalSchedulingHandler extends CommonConfigHandler<GlobalSchedulingConfigEntity, GlobalSchedulingConfigRestModel, GlobalSchedulingRepository> {

    public GlobalSchedulingHandler(final Class<GlobalSchedulingConfigEntity> databaseEntityClass, final Class<GlobalSchedulingConfigRestModel> configRestModelClass, final GlobalSchedulingConfigActions configActions,
            final ContentConverter contentConverter) {
        super(databaseEntityClass, configRestModelClass, configActions, contentConverter);
    }
}
