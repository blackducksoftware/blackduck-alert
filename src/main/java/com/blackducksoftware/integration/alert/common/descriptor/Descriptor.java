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
package com.blackducksoftware.integration.alert.common.descriptor;

import java.util.Map;
import java.util.Set;

import com.blackducksoftware.integration.alert.database.RepositoryAccessor;
import com.blackducksoftware.integration.alert.database.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.web.model.ConfigRestModel;
import com.blackducksoftware.integration.alert.workflow.startup.AlertStartupProperty;
import com.blackducksoftware.integration.exception.IntegrationException;

public abstract class Descriptor {
    private final String name;
    private final String label;
    private final DescriptorType type;
    private final DatabaseContentConverter contentConverter;
    private final RepositoryAccessor repositoryAccessor;

    public Descriptor(final String name, final String label, final DescriptorType type, final DatabaseContentConverter contentConverter, final RepositoryAccessor repositoryAccessor) {
        this.name = name;
        this.label = label;
        this.type = type;
        this.contentConverter = contentConverter;
        this.repositoryAccessor = repositoryAccessor;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public DescriptorType getType() {
        return type;
    }

    public RepositoryAccessor getGlobalRepositoryAccessor() {
        return repositoryAccessor;
    }

    public DatabaseContentConverter getGlobalContentConverter() {
        return contentConverter;
    }

    public abstract Set<AlertStartupProperty> getGlobalEntityPropertyMapping();

    public abstract ConfigRestModel getGlobalRestModelObject();

    public abstract void validateGlobalConfig(ConfigRestModel restModel, Map<String, String> fieldErrors);

    public abstract void testGlobalConfig(DatabaseEntity entity) throws IntegrationException;

}
