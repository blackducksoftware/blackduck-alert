package com.synopsys.integration.alert.component.settings.proxy.action;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.api.ConfigurationCrudHelper;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.synopsys.integration.alert.component.settings.proxy.database.accessor.SettingsProxyConfigAccessor;
import com.synopsys.integration.alert.component.settings.proxy.model.SettingsProxyModel;
import com.synopsys.integration.alert.component.settings.proxy.validator.SettingsProxyValidator;

@Component
public class SettingsProxyCrudActions {
    private final ConfigurationCrudHelper configurationHelper;
    private final SettingsProxyConfigAccessor configurationAccessor;
    private final SettingsProxyValidator validator;

    @Autowired
    public SettingsProxyCrudActions(AuthorizationManager authorizationManager, SettingsProxyConfigAccessor configurationAccessor, SettingsProxyValidator validator, SettingsDescriptorKey settingsDescriptorKey) {
        this.configurationHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, settingsDescriptorKey);
        this.configurationAccessor = configurationAccessor;
        this.validator = validator;
    }

    public ActionResponse<SettingsProxyModel> getOne(UUID id) {
        return configurationHelper.getOne(() -> configurationAccessor.getConfiguration(id));
    }

    public ActionResponse<AlertPagedModel<SettingsProxyModel>> getPaged(int page, int size) {
        return configurationHelper.getPage(() -> configurationAccessor.getConfigurationPage(page, size));
    }

    public ActionResponse<SettingsProxyModel> create(SettingsProxyModel resource) {
        return configurationHelper.create(
            () -> validator.validate(resource),
            () -> configurationAccessor.createConfiguration(resource)
        );
    }

    public ActionResponse<SettingsProxyModel> update(UUID id, SettingsProxyModel requestResource) {
        return configurationHelper.update(
            () -> validator.validate(requestResource),
            () -> configurationAccessor.getConfiguration(id).isPresent(),
            () -> configurationAccessor.updateConfiguration(id, requestResource)
        );
    }

    public ActionResponse<SettingsProxyModel> delete(UUID id) {
        return configurationHelper.delete(
            () -> configurationAccessor.getConfiguration(id).isPresent(),
            () -> configurationAccessor.deleteConfiguration(id)
        );
    }
}
