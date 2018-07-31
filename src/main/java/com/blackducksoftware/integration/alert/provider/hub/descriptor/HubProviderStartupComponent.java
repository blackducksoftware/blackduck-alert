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
package com.blackducksoftware.integration.alert.provider.hub.descriptor;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.common.descriptor.config.StartupComponent;
import com.blackducksoftware.integration.alert.database.entity.EntityPropertyMapper;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalBlackDuckConfigEntity;
import com.blackducksoftware.integration.alert.web.provider.blackduck.GlobalBlackDuckConfig;
import com.blackducksoftware.integration.alert.workflow.startup.AlertStartupProperty;

@Component
public class ProviderStartupComponent extends StartupComponent {
    private final EntityPropertyMapper entityPropertyMapper;

    @Autowired
    public HubProviderStartupComponent(final EntityPropertyMapper entityPropertyMapper) {
        super(new GlobalBlackDuckConfig());
        this.entityPropertyMapper = entityPropertyMapper;
    }

    @Override
    public Set<AlertStartupProperty> getGlobalEntityPropertyMapping() {
        return entityPropertyMapper.mapEntityToProperties(BlackDuckDescriptor.PROVIDER_NAME, GlobalBlackDuckConfigEntity.class);
    }

}
