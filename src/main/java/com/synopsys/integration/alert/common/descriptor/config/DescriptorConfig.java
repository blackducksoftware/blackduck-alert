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
package com.synopsys.integration.alert.common.descriptor.config;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.synopsys.integration.alert.database.RepositoryAccessor;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.exception.IntegrationException;

public abstract class DescriptorConfig {
    private final TypeConverter typeConverter;
    private final RepositoryAccessor repositoryAccessor;
    private final StartupComponent startupComponent;

    public DescriptorConfig(final TypeConverter typeConverter, final RepositoryAccessor repositoryAccessor) {
        this(typeConverter, repositoryAccessor, null);
    }

    public DescriptorConfig(final TypeConverter typeConverter, final RepositoryAccessor repositoryAccessor, final StartupComponent startupComponent) {
        this.typeConverter = typeConverter;
        this.repositoryAccessor = repositoryAccessor;
        this.startupComponent = startupComponent;
    }

    public TypeConverter getTypeConverter() {
        return typeConverter;
    }

    public RepositoryAccessor getRepositoryAccessor() {
        return repositoryAccessor;
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

    public Optional<? extends DatabaseEntity> readEntity(final long id) {
        return getRepositoryAccessor().readEntity(id);
    }

    public List<? extends DatabaseEntity> readEntities() {
        return getRepositoryAccessor().readEntities();
    }

    public DatabaseEntity saveEntity(final DatabaseEntity entity) {
        return getRepositoryAccessor().saveEntity(entity);
    }

    public void deleteEntity(final long id) {
        getRepositoryAccessor().deleteEntity(id);
    }

    public DatabaseEntity populateEntityFromConfig(final Config config) {
        return getTypeConverter().populateEntityFromConfig(config);
    }

    public Config populateConfigFromEntity(final DatabaseEntity entity) {
        return getTypeConverter().populateConfigFromEntity(entity);
    }

    public Config getConfigFromJson(final String json) {
        return getTypeConverter().getConfigFromJson(json);
    }

}
