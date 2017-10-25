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
package com.blackducksoftware.integration.hub.alert.web.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.web.model.ChannelRestModel;

public interface ConfigController<D extends DatabaseEntity, R extends ChannelRestModel> {
    public List<R> getConfig(final Long id);

    public ResponseEntity<String> postConfig(final R restModel);

    public ResponseEntity<String> putConfig(final R restModel);

    public ResponseEntity<String> deleteConfig(final R restModel);

    public ResponseEntity<String> testConfig(final R restModel);

    public D restModelToDatabaseModel(final R restModel);

    public R databaseModelToRestModel(final D databaseModel);

    public List<R> databaseModelsToRestModels(final List<D> databaseModels);
}
