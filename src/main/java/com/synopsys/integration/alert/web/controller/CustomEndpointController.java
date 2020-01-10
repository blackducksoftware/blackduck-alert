/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.web.controller;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.action.CustomEndpointManager;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

@RestController
@RequestMapping(CustomEndpointManager.CUSTOM_ENDPOINT_URL)
public class CustomEndpointController {

    private final CustomEndpointManager customEndpointManager;
    private final ResponseFactory responseFactory;
    private final AuthorizationManager authorizationManager;

    @Autowired
    public CustomEndpointController(CustomEndpointManager customEndpointManager, ResponseFactory responseFactory, AuthorizationManager authorizationManager) {
        this.customEndpointManager = customEndpointManager;
        this.responseFactory = responseFactory;
        this.authorizationManager = authorizationManager;
    }

    @PostMapping("/{key}")
    public ResponseEntity<String> postConfig(@PathVariable String key, @RequestBody FieldModel restModel) {
        if (!authorizationManager.hasExecutePermission(restModel.getContext(), restModel.getDescriptorName())) {
            return responseFactory.createForbiddenResponse();
        }

        if (StringUtils.isBlank(key)) {
            return responseFactory.createBadRequestResponse("", "Must be given the key associated with the custom functionality.");
        }

        Map<String, FieldValueModel> keyToValues = restModel.getKeyToValues();
        return customEndpointManager.performFunction(key, keyToValues);
    }

}
