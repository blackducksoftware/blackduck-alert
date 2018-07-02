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

import org.springframework.data.jpa.repository.JpaRepository;

import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.web.actions.SimpleConfigActions;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

public interface Descriptor {

    public String getName();

    public DescriptorType getType();

    public <E extends DatabaseEntity> Class<E> getGlobalEntityClass();

    public <R extends ConfigRestModel> Class<R> getGlobalRestModelClass();

    public <R extends JpaRepository<DatabaseEntity, Long>> R getGlobalRepository();

    public <A extends SimpleConfigActions> A getGlobalConfigActions();
}
