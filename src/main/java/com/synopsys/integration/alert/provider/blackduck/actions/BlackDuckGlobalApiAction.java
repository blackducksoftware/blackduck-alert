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
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ApiAction;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderGlobalUIConfig;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.provider.lifecycle.ProviderLifecycleManager;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;

@Component
public class BlackDuckGlobalApiAction extends ApiAction {
    private ProviderLifecycleManager providerLifecycleManager;
    private final ProviderDataAccessor providerDataAccessor;
    private final BlackDuckProvider blackDuckProvider;
    private final ConfigurationFieldModelConverter configurationFieldModelConverter;

    public BlackDuckGlobalApiAction(BlackDuckProvider blackDuckProvider, ProviderLifecycleManager providerLifecycleManager, ProviderDataAccessor providerDataAccessor,
        ConfigurationFieldModelConverter configurationFieldModelConverter) {
        this.blackDuckProvider = blackDuckProvider;
        this.providerLifecycleManager = providerLifecycleManager;
        this.providerDataAccessor = providerDataAccessor;
        this.configurationFieldModelConverter = configurationFieldModelConverter;
    }

    @Override
    public FieldModel afterSaveAction(FieldModel fieldModel) throws AlertException {
        handleNewOrUpdatedConfig(fieldModel);
        return super.afterSaveAction(fieldModel);
    }

    @Override
    public FieldModel afterUpdateAction(FieldModel fieldModel) throws AlertException {
        handleNewOrUpdatedConfig(fieldModel);
        return super.afterUpdateAction(fieldModel);
    }

    @Override
    public void afterDeleteAction(FieldModel fieldModel) {
        Map<String, FieldValueModel> keyToValues = fieldModel.getKeyToValues();
        FieldValueModel fieldValueModel = keyToValues.get(ProviderGlobalUIConfig.KEY_PROVIDER_CONFIG_NAME);
        String blackDuckGlobalConfigName = fieldValueModel.getValue().orElse("");

        Long configId = Long.parseLong(Objects.requireNonNullElse(fieldModel.getId(), "-1"));
        providerLifecycleManager.unscheduleTasksForProviderConfig(blackDuckProvider, configId);

        List<ProviderProject> blackDuckProjects = providerDataAccessor.getProjectsByProviderConfigName(blackDuckGlobalConfigName);
        providerDataAccessor.deleteProjects(blackDuckProjects);
    }

    private void handleNewOrUpdatedConfig(FieldModel fieldModel) throws AlertException {
        ConfigurationModel blackDuckGlobalConfiguration = configurationFieldModelConverter.convertToConfigurationModel(fieldModel);
        boolean valid = blackDuckProvider.validate(blackDuckGlobalConfiguration);
        if (valid) {
            providerLifecycleManager.scheduleTasksForProviderConfig(blackDuckProvider, blackDuckGlobalConfiguration);
        }
    }

}
