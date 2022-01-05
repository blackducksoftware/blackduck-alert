/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.action;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.api.provider.lifecycle.ProviderSchedulingManager;
import com.synopsys.integration.alert.common.action.ApiAction;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckCacheHttpClientCache;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;

@Component
public class BlackDuckGlobalApiAction extends ApiAction {
    private final ProviderSchedulingManager providerLifecycleManager;
    private final ProviderDataAccessor providerDataAccessor;
    private final BlackDuckProvider blackDuckProvider;
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
    private final BlackDuckCacheHttpClientCache blackDuckCacheHttpClientCache;

    public BlackDuckGlobalApiAction(
        BlackDuckProvider blackDuckProvider,
        ProviderSchedulingManager providerLifecycleManager,
        ProviderDataAccessor providerDataAccessor,
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor,
        BlackDuckCacheHttpClientCache blackDuckCacheHttpClientCache
    ) {
        this.blackDuckProvider = blackDuckProvider;
        this.providerLifecycleManager = providerLifecycleManager;
        this.providerDataAccessor = providerDataAccessor;
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
        this.blackDuckCacheHttpClientCache = blackDuckCacheHttpClientCache;
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
        blackDuckCacheHttpClientCache.invalidate(Long.parseLong(previousFieldModel.getId()));
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
            Optional<ConfigurationModel> retrievedConfig = configurationModelConfigurationAccessor.getProviderConfigurationByName(providerConfigName.get());
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
