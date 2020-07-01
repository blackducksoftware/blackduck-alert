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
package com.synopsys.integration.alert.provider.blackduck.actions;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ApiAction;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.provider.helper.ProviderAfterUpdateActionHelper;
import com.synopsys.integration.alert.common.provider.lifecycle.ProviderSchedulingManager;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;

@Component
public class BlackDuckGlobalApiAction extends ApiAction {
    private final ProviderSchedulingManager providerLifecycleManager;
    private final ProviderDataAccessor providerDataAccessor;
    private final BlackDuckProvider blackDuckProvider;
    private final ConfigurationAccessor configurationAccessor;
    private final ProviderAfterUpdateActionHelper providerAfterUpdateActionHelper;

    public BlackDuckGlobalApiAction(BlackDuckProvider blackDuckProvider, ProviderSchedulingManager providerLifecycleManager, ProviderDataAccessor providerDataAccessor,
        ConfigurationAccessor configurationAccessor, ProviderAfterUpdateActionHelper providerAfterUpdateActionHelper) {
        this.blackDuckProvider = blackDuckProvider;
        this.providerLifecycleManager = providerLifecycleManager;
        this.providerDataAccessor = providerDataAccessor;
        this.configurationAccessor = configurationAccessor;
        this.providerAfterUpdateActionHelper = providerAfterUpdateActionHelper;
    }

    @Override
    public FieldModel beforeUpdateAction(FieldModel fieldModel) throws AlertException {
        Long configId = Long.parseLong(fieldModel.getId());
        providerLifecycleManager.unscheduleTasksForProviderConfig(configId);
        return super.beforeUpdateAction(fieldModel);
    }

    @Override
    public FieldModel afterSaveAction(FieldModel fieldModel) throws AlertException {
        handleNewOrUpdatedConfig(fieldModel);
        return super.afterSaveAction(fieldModel);
    }

    @Override
    public FieldModel afterUpdateAction(FieldModel previousFieldModel, FieldModel currentFieldModel) throws AlertException {
        handleNewOrUpdatedConfig(currentFieldModel);
        providerAfterUpdateActionHelper.updateDistributionJobsWithNewProviderName(previousFieldModel, currentFieldModel);
        return super.afterUpdateAction(previousFieldModel, currentFieldModel);
    }

    @Override
    public void afterDeleteAction(FieldModel fieldModel) {
        Map<String, FieldValueModel> keyToValues = fieldModel.getKeyToValues();
        FieldValueModel fieldValueModel = keyToValues.get(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        String blackDuckGlobalConfigName = fieldValueModel.getValue().orElse("");

        Long configId = Long.parseLong(fieldModel.getId());
        providerLifecycleManager.unscheduleTasksForProviderConfig(configId);

        List<ProviderProject> blackDuckProjects = providerDataAccessor.getProjectsByProviderConfigName(blackDuckGlobalConfigName);
        providerDataAccessor.deleteProjects(blackDuckProjects);
    }

    private void handleNewOrUpdatedConfig(FieldModel currentFieldModel) throws AlertException {
        Optional<String> providerConfigName = currentFieldModel.getFieldValue(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        if (providerConfigName.isPresent()) {
            Optional<ConfigurationModel> retrievedConfig = configurationAccessor.getProviderConfigurationByName(providerConfigName.get());
            if (retrievedConfig.isPresent()) {
                ConfigurationModel blackDuckGlobalConfig = retrievedConfig.get();
                boolean valid = blackDuckProvider.validate(blackDuckGlobalConfig);
                boolean enabled = blackDuckGlobalConfig.getField(ProviderDescriptor.KEY_PROVIDER_CONFIG_ENABLED)
                                      .flatMap(ConfigurationFieldModel::getFieldValue)
                                      .map(Boolean::parseBoolean)
                                      .orElse(false);
                if (valid && enabled) {
                    providerLifecycleManager.scheduleTasksForProviderConfig(blackDuckProvider, retrievedConfig.get());
                }
            }
        }
    }

}
