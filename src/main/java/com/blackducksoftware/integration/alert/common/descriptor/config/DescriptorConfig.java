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
package com.blackducksoftware.integration.alert.common.descriptor.config;

import java.util.Map;

import com.blackducksoftware.integration.alert.database.RepositoryAccessor;
import com.blackducksoftware.integration.alert.database.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.web.model.Config;
import com.blackducksoftware.integration.exception.IntegrationException;

public abstract class DescriptorConfig {
    private final TypeConverter typeConverter;
    private final RepositoryAccessor repositoryAccessor;
    private StartupComponent startupComponent;

    public DescriptorConfig(final TypeConverter typeConverter, final RepositoryAccessor repositoryAccessor) {
        this.typeConverter = typeConverter;
        this.repositoryAccessor = repositoryAccessor;
        this.startupComponent = null;
    }

    public TypeConverter getTypeConverter() {
        return typeConverter;
    }

    public RepositoryAccessor getRepositoryAccessor() {
        return repositoryAccessor;
    }

    public void setStartupComponent(final StartupComponent startupComponent) {
        this.startupComponent = startupComponent;
    }

    public StartupComponent getStartupComponent() {
        return startupComponent;
    }

    public boolean hasStartupProperties() {
        return getStartupComponent() != null;
    }

    public abstract UIComponent getUiComponent();

    public abstract void validateConfig(Config restModel, Map<String, String> fieldErrors);

    public abstract void testConfig(DatabaseEntity entity) throws IntegrationException;

}
