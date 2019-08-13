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
package com.synopsys.integration.alert.web.provider;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOption;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.database.api.DefaultProviderDataAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
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
    public ResponseEntity<String> getProjects(@PathVariable(name = "provider") String provider) {
        if (StringUtils.isBlank(provider)) {
            logger.debug("Received provider project data request with a blank provider");
            return responseFactory.createMessageResponse(HttpStatus.BAD_REQUEST, "The specified provider must not be blank");
        }
        try {
            List<ProviderProject> projects = providerDataAccessor.findByProviderName(provider);
            if (projects.isEmpty()) {
                logger.info("No projects found in the database for the provider: {}", provider);
            }
            String usersJson = contentConverter.getJsonString(projects);
            return responseFactory.createOkContentResponse(usersJson);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return responseFactory.createMessageResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping(value = "{provider}/users/emails")
    public ResponseEntity<String> getUserEmails(
        @PathVariable(name = "provider") String provider,
        @RequestParam(value = "offset", required = false) Integer offset,
        @RequestParam(value = "limit", required = false) Integer limit,
        @RequestParam(value = "q", required = false) String q) {
        if (StringUtils.isBlank(provider)) {
            logger.debug("Received provider user email data request with a blank provider");
            return responseFactory.createMessageResponse(HttpStatus.BAD_REQUEST, "The specified provider must not be blank");
        }

        try {
            final AlertPagedModel<ProviderUserModel> pageOfUsers = providerDataAccessor.getPageOfUsers(BlackDuckProvider.COMPONENT_NAME, offset, limit, q);
            final LinkedHashSet<LabelValueSelectOption> emailOptions = pageOfUsers.getContent()
                                                                           .stream()
                                                                           .map(ProviderUserModel::getEmailAddress)
                                                                           .sorted()
                                                                           .map(LabelValueSelectOption::new)
                                                                           .collect(Collectors.toCollection(LinkedHashSet::new));
            if (emailOptions.isEmpty()) {
                logger.info("No user emails found in the database for the provider: {}", provider);
            }
            final String usersJson = contentConverter.getJsonString(emailOptions);
            return responseFactory.createOkContentResponse(usersJson);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return responseFactory.createMessageResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
