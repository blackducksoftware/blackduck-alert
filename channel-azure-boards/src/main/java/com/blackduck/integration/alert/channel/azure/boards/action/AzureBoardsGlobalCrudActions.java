/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.action;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.channel.azure.boards.database.accessor.AzureBoardsGlobalConfigAccessor;
import com.blackduck.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.blackduck.integration.alert.channel.azure.boards.validator.AzureBoardsGlobalConfigurationValidator;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.rest.api.ConfigurationCrudHelper;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;

@Component
public class AzureBoardsGlobalCrudActions {
    private final ConfigurationCrudHelper configurationHelper;
    private final AzureBoardsGlobalConfigAccessor configurationAccessor;
    private final AzureBoardsGlobalConfigurationValidator configurationValidator;

    @Autowired
    public AzureBoardsGlobalCrudActions(
        AuthorizationManager authorizationManager,
        AzureBoardsGlobalConfigAccessor configurationAccessor,
        AzureBoardsGlobalConfigurationValidator configurationValidator
    ) {
        this.configurationHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, ChannelKeys.AZURE_BOARDS);
        this.configurationAccessor = configurationAccessor;
        this.configurationValidator = configurationValidator;
    }

    public ActionResponse<AzureBoardsGlobalConfigModel> getOne(UUID id) {
        return configurationHelper.getOne(() -> configurationAccessor.getConfiguration(id));
    }

    public ActionResponse<AlertPagedModel<AzureBoardsGlobalConfigModel>> getPaged(
        int page, int size, String searchTerm, String sortName, String sortOrder
    ) {
        return configurationHelper.getPage(() -> configurationAccessor.getConfigurationPage(page, size, searchTerm, sortName, sortOrder));
    }

    public ActionResponse<AzureBoardsGlobalConfigModel> create(AzureBoardsGlobalConfigModel resource) {
        return configurationHelper.create(
            () -> configurationValidator.validate(resource, null),
            () -> configurationAccessor.existsConfigurationByName(resource.getName()),
            () -> configurationAccessor.createConfiguration(resource)
        );
    }

    public ActionResponse<AzureBoardsGlobalConfigModel> update(UUID id, AzureBoardsGlobalConfigModel resource) {
        return configurationHelper.update(
            () -> configurationValidator.validate(resource, id.toString()),
            () -> configurationAccessor.existsConfigurationById(id),
            () -> configurationAccessor.updateConfiguration(id, resource)
        );
    }

    public ActionResponse<AzureBoardsGlobalConfigModel> delete(UUID id) {
        return configurationHelper.delete(
            () -> configurationAccessor.existsConfigurationById(id),
            () -> configurationAccessor.deleteConfiguration(id)
        );
    }
}
