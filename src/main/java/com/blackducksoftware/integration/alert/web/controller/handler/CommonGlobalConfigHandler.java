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
package com.blackducksoftware.integration.alert.web.controller.handler;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.blackducksoftware.integration.alert.ContentConverter;
import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.web.actions.ConfigActions;
import com.blackducksoftware.integration.alert.web.model.ConfigRestModel;

// TODO verify if this class is still necessary with our universal controllers
public class CommonGlobalConfigHandler<D extends DatabaseEntity, R extends ConfigRestModel, W extends JpaRepository<D, Long>> extends CommonConfigHandler<D, R, W> {
    private final Class<D> databaseEntityClass;

    public CommonGlobalConfigHandler(final Class<D> databaseEntityClass, final Class<R> configRestModelClass, final ConfigActions<D, R, W> configActions, final ContentConverter contentConverter) {
        super(databaseEntityClass, configRestModelClass, configActions, contentConverter);
        this.databaseEntityClass = databaseEntityClass;
    }

    @Override
    public ResponseEntity<String> postConfig(final R restModel) {
        if (!configActions.getRepository().findAll().isEmpty()) {
            return createResponse(HttpStatus.PRECONDITION_FAILED, String.format("Cannot POST because a global configuration for %s already exists!", databaseEntityClass.getSimpleName()));
        }
        return super.postConfig(restModel);
    }

}
