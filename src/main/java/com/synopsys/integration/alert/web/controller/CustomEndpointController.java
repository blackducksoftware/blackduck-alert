/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.action.ConfigurationAction;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.action.CustomEndpointAction;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

@RestController
@RequestMapping(value = CustomEndpointController.CUSTOM_ENDPOINT_URL)
public class CustomEndpointController {
    public static final String CUSTOM_ENDPOINT_URL = BaseController.BASE_PATH + "/custom";

    private final List<ConfigurationAction> configurationActions;
    private final ResponseFactory responseFactory;

    @Autowired
    public CustomEndpointController(final List<ConfigurationAction> configurationActions, final ResponseFactory responseFactory) {
        this.configurationActions = configurationActions;
        this.responseFactory = responseFactory;
    }

    @PostMapping("/{key}")
    public ResponseEntity<String> postConfig(@PathVariable final String key, @RequestBody final FieldModel restModel) {
        if (StringUtils.isBlank(key)) {
            return responseFactory.createBadRequestResponse("", "Must be given the key associated with the custom functionality.");
        }
        final String descriptorName = restModel.getDescriptorName();
        final Optional<ConfigurationAction> actions = configurationActions.stream()
                                                          .filter(configurationAction -> configurationAction.getDescriptorName().equals(descriptorName))
                                                          .findFirst();
        if (actions.isPresent()) {
            final CustomEndpointAction customEndpointAction = actions.get().getCustomEndpointAction(key);
            final String id = restModel.getId();
            final Map<String, FieldValueModel> keyToValues = restModel.getKeyToValues();
            try {
                final String endpointResponse = customEndpointAction.performAction(keyToValues);
                responseFactory.createOkResponse(id, endpointResponse);
            } catch (final AlertException e) {
                return responseFactory.createBadRequestResponse(id, e.getMessage());
            }
        }
        return responseFactory.createBadRequestResponse("", "Did not find related ConfigurationAction.");
    }
}
