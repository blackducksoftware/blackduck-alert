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
package com.synopsys.integration.alert.channel.email.web;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.endpoint.CustomEndpoint;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;
import com.synopsys.integration.alert.common.rest.HttpServletContentWrapper;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

@Component
public class EmailCustomEndpoint extends CustomEndpoint<List<ProviderUserModel>> {
    private final Logger logger = LoggerFactory.getLogger(EmailCustomEndpoint.class);
    private ProviderDataAccessor providerDataAccessor;

    @Autowired
    public EmailCustomEndpoint(AuthorizationManager authorizationManager, ProviderDataAccessor providerDataAccessor) {
        super(authorizationManager);
        this.providerDataAccessor = providerDataAccessor;
    }

    @Override
    public ActionResponse<List<ProviderUserModel>> createActionResponse(FieldModel fieldModel, HttpServletContentWrapper servletContentWrapper) {
        String providerConfigName = fieldModel.getFieldValue(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME).orElse("");

        if (StringUtils.isBlank(providerConfigName)) {
            logger.debug("Received provider user email data request with a blank provider config name");
            return new ActionResponse<>(HttpStatus.BAD_REQUEST, "You must select a provider config to populate data.");
        }

        try {
            List<ProviderUserModel> pageOfUsers = providerDataAccessor.getUsersByProviderConfigName(providerConfigName);
            if (pageOfUsers.isEmpty()) {
                logger.info("No user emails found in the database for the provider: {}", providerConfigName);
            }
            return new ActionResponse<>(HttpStatus.OK, pageOfUsers);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
