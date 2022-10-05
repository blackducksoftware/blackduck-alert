package com.synopsys.integration.alert.channel.azure.boards.action;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.channel.azure.boards.database.accessor.AzureBoardsGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.api.ConfigurationCrudHelper;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AzureBoardsGlobalCrudActions {
    private final ConfigurationCrudHelper configurationHelper;
    private final AzureBoardsGlobalConfigAccessor configurationAccessor;

    @Autowired
    public AzureBoardsGlobalCrudActions(
        AuthorizationManager authorizationManager,
        AzureBoardsGlobalConfigAccessor configurationAccessor
        //TODO: AzureBoardsGlobalConfigurationValidator validator
    ) {
        this.configurationHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, ChannelKeys.AZURE_BOARDS);
        this.configurationAccessor = configurationAccessor;
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
            () -> ValidationResponseModel.success(), //TODO: validator.validate(resource, null)
            () -> configurationAccessor.existsConfigurationByName(resource.getName()),
            () -> configurationAccessor.createConfiguration(resource)
        );
    }

    public ActionResponse<AzureBoardsGlobalConfigModel> update(UUID id, AzureBoardsGlobalConfigModel resource) {
        return configurationHelper.update(
            () -> ValidationResponseModel.success(), //TODO: validator.validate(resource, null),
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
