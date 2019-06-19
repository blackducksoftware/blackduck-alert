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
package com.synopsys.integration.alert.channel.jira.web;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.descriptor.JiraDescriptor;
import com.synopsys.integration.alert.common.action.CustomEndpointManager;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.web.controller.ResponseFactory;

@Component
public class JiraCustomEndpoint {
    private final CustomEndpointManager customEndpointManager;
    private final ResponseFactory responseFactory;

    @Autowired
    public JiraCustomEndpoint(final CustomEndpointManager customEndpointManager, final ResponseFactory responseFactory) throws AlertException {
        this.customEndpointManager = customEndpointManager;
        this.responseFactory = responseFactory;

        registerEndpoints();
    }

    public void registerEndpoints() throws AlertException {
        customEndpointManager.registerFunction(JiraDescriptor.KEY_JIRA_CONFIGURE_PLUGIN, this::installJiraPlugin);
    }

    public ResponseEntity<String> installJiraPlugin(final Map<String, FieldValueModel> fieldValueModels) {
        return responseFactory.createMethodNotAllowedResponse("Has not yet been implemented");
    }

}
