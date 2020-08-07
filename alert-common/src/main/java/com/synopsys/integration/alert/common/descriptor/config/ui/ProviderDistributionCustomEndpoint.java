/**
 * alert-common
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
package com.synopsys.integration.alert.common.descriptor.config.ui;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.action.CustomEndpointManager;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.table.TableSelectCustomEndpoint;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.model.FieldModel;

@Component
public class ProviderDistributionCustomEndpoint extends TableSelectCustomEndpoint {
    private static final String MISSING_PROVIDER_ERROR = "Provider name is required to retrieve projects.";

    private final ProviderDataAccessor providerDataAccessor;

    @Autowired
    public ProviderDistributionCustomEndpoint(CustomEndpointManager customEndpointManager, ProviderDataAccessor providerDataAccessor, ResponseFactory responseFactory, Gson gson) throws AlertException {
        super(ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT, customEndpointManager, responseFactory, gson);
        this.providerDataAccessor = providerDataAccessor;
    }

    @Override
    protected List<?> createData(FieldModel fieldModel) throws ResponseStatusException {
        String providerName = fieldModel.getFieldValue(ChannelDistributionUIConfig.KEY_PROVIDER_NAME).orElse("");
        if (StringUtils.isBlank(providerName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MISSING_PROVIDER_ERROR);
        }

        String providerConfigName = fieldModel.getFieldValue(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME).orElse("");
        return providerDataAccessor.getProjectsByProviderConfigName(providerConfigName);
    }

}
