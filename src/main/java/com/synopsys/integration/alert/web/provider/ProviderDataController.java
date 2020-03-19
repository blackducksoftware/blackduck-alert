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
package com.synopsys.integration.alert.web.provider;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.database.api.DefaultProviderDataAccessor;
import com.synopsys.integration.alert.web.controller.BaseController;
import com.synopsys.integration.alert.web.controller.ResponseFactory;

@RestController
@RequestMapping(BaseController.BASE_PATH + "/provider")
public class ProviderDataController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(ProviderDataController.class);

    private final ResponseFactory responseFactory;
    private final DefaultProviderDataAccessor providerDataAccessor;
    private final ContentConverter contentConverter;

    @Autowired
    public ProviderDataController(final ResponseFactory responseFactory, final DefaultProviderDataAccessor providerDataAccessor, final ContentConverter contentConverter) {
        this.responseFactory = responseFactory;
        this.providerDataAccessor = providerDataAccessor;
        this.contentConverter = contentConverter;
    }

    @GetMapping(value = "{provider}/projects")
    public ResponseEntity<String> getProjects(@PathVariable(name = "provider") final String provider) {
        if (StringUtils.isBlank(provider)) {
            logger.debug("Received provider project data request with a blank provider");
            return responseFactory.createMessageResponse(HttpStatus.BAD_REQUEST, "The specified provider must not be blank");
        }
        try {
            final List<ProviderProject> projects = providerDataAccessor.findByProviderName(provider);
            if (projects.isEmpty()) {
                logger.info("No projects found in the database for the provider: {}", provider);
            }
            final String usersJson = contentConverter.getJsonString(projects);
            return responseFactory.createOkContentResponse(usersJson);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return responseFactory.createMessageResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
