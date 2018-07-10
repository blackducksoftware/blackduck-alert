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
package com.blackducksoftware.integration.hub.alert.descriptor;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

public abstract class Descriptor {
    private final String name;
    private final DescriptorType type;

    public Descriptor(final String name, final DescriptorType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public DescriptorType getType() {
        return type;
    }

    public abstract Field[] getGlobalEntityFields();

    public abstract ConfigRestModel getGlobalRestModelObject();

    public abstract List<? extends DatabaseEntity> readGlobalEntities();

    public abstract Optional<? extends DatabaseEntity> readGlobalEntity(long id);

    public abstract Optional<? extends DatabaseEntity> saveGlobalEntity(DatabaseEntity entity);

    public abstract void deleteGlobalEntity(long id);

    public abstract ConfigRestModel convertFromStringToGlobalRestModel(String json);

    public abstract DatabaseEntity convertFromGlobalRestModelToGlobalConfigEntity(ConfigRestModel restModel) throws AlertException;

    public abstract ConfigRestModel convertFromGlobalEntityToGlobalRestModel(DatabaseEntity entity) throws AlertException;

    public abstract void validateGlobalConfig(ConfigRestModel restModel, Map<String, String> fieldErrors);

    public abstract void testGlobalConfig(DatabaseEntity entity) throws IntegrationException;
}
