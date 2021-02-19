/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.provider.blackduck.action;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.action.ApiAction;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.provider.state.StatefulProvider;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.JobProviderProjectFieldModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckCacheHttpClientCache;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.action.task.AddUserToProjectsRunnable;

@Component
public class BlackDuckDistributionApiAction extends ApiAction {
    private final ConfigurationAccessor configurationAccessor;
    private final BlackDuckProvider blackDuckProvider;
    private final BlackDuckCacheHttpClientCache blackDuckHttpClientCache;
    private final Gson gson;

    @Autowired
    public BlackDuckDistributionApiAction(ConfigurationAccessor configurationAccessor, BlackDuckProvider blackDuckProvider, BlackDuckCacheHttpClientCache blackDuckHttpClientCache,
        Gson gson) {
        this.configurationAccessor = configurationAccessor;
        this.blackDuckProvider = blackDuckProvider;
        this.blackDuckHttpClientCache = blackDuckHttpClientCache;
        this.gson = gson;
    }

    @Override
    public FieldModel afterSaveAction(FieldModel fieldModel) throws AlertException {
        afterWrite(fieldModel);
        return super.afterSaveAction(fieldModel);
    }

    @Override
    public FieldModel afterUpdateAction(FieldModel previousFieldModel, FieldModel currentFieldModel) throws AlertException {
        afterWrite(currentFieldModel);
        return super.afterUpdateAction(previousFieldModel, currentFieldModel);
    }

    private void afterWrite(FieldModel currentFieldModel) throws AlertException {
        Optional<Long> optionalProviderConfigId = currentFieldModel.getFieldValue(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID).map(Long::valueOf);
        boolean filterByProject = extractFilterByProject(currentFieldModel);
        Collection<JobProviderProjectFieldModel> configuredProjects = filterByProject ? extractConfiguredProjects(currentFieldModel) : List.of();
        if (optionalProviderConfigId.isPresent()) {
            Optional<ConfigurationModel> optionalBlackDuckGlobalConfig = configurationAccessor.getConfigurationById(optionalProviderConfigId.get());
            if (optionalBlackDuckGlobalConfig.isPresent()) {
                StatefulProvider statefulProvider = blackDuckProvider.createStatefulProvider(optionalBlackDuckGlobalConfig.get());

                AddUserToProjectsRunnable addUserToProjects = new AddUserToProjectsRunnable(blackDuckHttpClientCache, (BlackDuckProperties) statefulProvider.getProperties(), filterByProject, configuredProjects);
                addUserToProjects.run();
            }
        }
    }

    private Collection<JobProviderProjectFieldModel> extractConfiguredProjects(FieldModel currentFieldModel) {
        Collection<String> providerProjectJsonStrings = currentFieldModel.getFieldValueModel(ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT)
                                                            .map(FieldValueModel::getValues)
                                                            .orElse(Set.of());
        return providerProjectJsonStrings.stream()
                   .filter(Objects::nonNull)
                   .map(this::convertJsonToProjectModel)
                   .collect(Collectors.toSet());
    }

    private JobProviderProjectFieldModel convertJsonToProjectModel(String json) {
        return gson.fromJson(json, JobProviderProjectFieldModel.class);
    }

    private boolean extractFilterByProject(FieldModel currentFieldModel) {
        return currentFieldModel.getFieldValueModel(ProviderDistributionUIConfig.KEY_FILTER_BY_PROJECT)
                   .flatMap(FieldValueModel::getValue)
                   .map(BooleanUtils::toBoolean)
                   .orElse(false);
    }

}
