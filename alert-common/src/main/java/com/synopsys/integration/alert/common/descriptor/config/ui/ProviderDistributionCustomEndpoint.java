/**
 * alert-common
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
package com.synopsys.integration.alert.common.descriptor.config.ui;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.action.CustomEndpointManager;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

@Component
public class ProviderDistributionCustomEndpoint {
    private static final String MISSING_PROVIDER_ERROR = "Provider name is required to retrieve projects.";

    private ProviderDataAccessor providerDataAccessor;
    private ResponseFactory responseFactory;
    private Gson gson;

    @Autowired
    public ProviderDistributionCustomEndpoint(final CustomEndpointManager customEndpointManager, final ProviderDataAccessor providerDataAccessor,
        final ResponseFactory responseFactory, final Gson gson) throws AlertException {
        this.providerDataAccessor = providerDataAccessor;
        this.responseFactory = responseFactory;
        this.gson = gson;

        customEndpointManager.registerFunction(ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT, this::createProjectListing);
    }

    public ResponseEntity<String> createProjectListing(Map<String, FieldValueModel> fieldValues) {
        final FieldValueModel fieldValueModel = fieldValues.get(ChannelDistributionUIConfig.KEY_PROVIDER_NAME);
        if (fieldValueModel == null) {
            return responseFactory.createBadRequestResponse("", MISSING_PROVIDER_ERROR);
        }

        final String providerName = fieldValueModel.getValue().orElse("");
        if (StringUtils.isBlank(providerName)) {
            return responseFactory.createBadRequestResponse("", MISSING_PROVIDER_ERROR);
        }

        final List<ProviderProject> byProviderName = providerDataAccessor.findByProviderName(providerName);
        String providerProjects = gson.toJson(byProviderName);
        return responseFactory.createOkContentResponse(providerProjects);
    }

}
