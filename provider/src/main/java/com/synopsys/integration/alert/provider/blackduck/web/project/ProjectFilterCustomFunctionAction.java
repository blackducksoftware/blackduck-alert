/**
 * provider
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
package com.synopsys.integration.alert.provider.blackduck.web.project;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.CustomFunctionAction;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.FieldValidationUtility;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.provider.state.StatefulProvider;
import com.synopsys.integration.alert.common.rest.HttpServletContentWrapper;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;
import com.synopsys.integration.log.Slf4jIntLogger;

@Component
public class ProjectFilterCustomFunctionAction extends CustomFunctionAction<BlackDuckProjectOptions> {
    private static final String MISSING_PROVIDER_ERROR = "Provider name is required to retrieve projects.";
    private static final ActionResponse<BlackDuckProjectOptions> NO_PROJECT_OPTIONS = new ActionResponse<>(HttpStatus.OK, new BlackDuckProjectOptions(List.of()));

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ConfigurationAccessor configurationAccessor;
    private final BlackDuckProvider blackDuckProvider;

    @Autowired
    public ProjectFilterCustomFunctionAction(AuthorizationManager authorizationManager, DescriptorMap descriptorMap, FieldValidationUtility fieldValidationUtility,
        ConfigurationAccessor configurationAccessor, BlackDuckProvider blackDuckProvider) {
        super(ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT, authorizationManager, descriptorMap, fieldValidationUtility);
        this.configurationAccessor = configurationAccessor;
        this.blackDuckProvider = blackDuckProvider;
    }

    @Override
    public ActionResponse<BlackDuckProjectOptions> createActionResponse(FieldModel fieldModel, HttpServletContentWrapper servletContentWrapper) {
        String providerName = fieldModel.getFieldValue(ChannelDistributionUIConfig.KEY_PROVIDER_NAME).orElse("");
        if (StringUtils.isBlank(providerName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MISSING_PROVIDER_ERROR);
        }

        return fieldModel.getFieldValue(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID)
                   .map(Long::parseLong)
                   .map(this::getBlackDuckProjectsActionResponse)
                   .orElse(NO_PROJECT_OPTIONS);
    }

    private ActionResponse<BlackDuckProjectOptions> getBlackDuckProjectsActionResponse(Long blackDuckGlobalConfigId) {
        try {
            Optional<ConfigurationModel> optionalBlackDuckGlobalConfig = configurationAccessor.getConfigurationById(blackDuckGlobalConfigId);
            if (optionalBlackDuckGlobalConfig.isPresent()) {
                StatefulProvider statefulProvider = blackDuckProvider.createStatefulProvider(optionalBlackDuckGlobalConfig.get());
                BlackDuckProperties properties = (BlackDuckProperties) statefulProvider.getProperties();

                Slf4jIntLogger intLogger = new Slf4jIntLogger(logger);
                BlackDuckHttpClient blackDuckHttpClient = properties.createBlackDuckHttpClient(intLogger);
                BlackDuckServicesFactory blackDuckServicesFactory = properties.createBlackDuckServicesFactory(blackDuckHttpClient, intLogger);

                ProjectService projectService = blackDuckServicesFactory.createProjectService();
                // FIXME improve performance
                List<BlackDuckProjectSelectOption> options = projectService.getAllProjects()
                                                                 .stream()
                                                                 .map(project -> new BlackDuckProjectSelectOption(project.getName(), project.getDescription()))
                                                                 .collect(Collectors.toList());
                return new ActionResponse<>(HttpStatus.OK, new BlackDuckProjectOptions(options));
            }
        } catch (Exception e) {
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        return NO_PROJECT_OPTIONS;
    }

}
