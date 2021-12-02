package com.synopsys.integration.alert.component.settings.proxy.web;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.api.ReadPageController;
import com.synopsys.integration.alert.common.rest.api.StaticConfigResourceController;
import com.synopsys.integration.alert.common.rest.api.ValidateController;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.component.settings.proxy.action.SettingsProxyCrudActions;
import com.synopsys.integration.alert.component.settings.proxy.action.SettingsProxyValidationAction;
import com.synopsys.integration.alert.component.settings.proxy.model.SettingsProxyModel;

@RestController
@RequestMapping(AlertRestConstants.SETTINGS_PROXY_PATH)
public class SettingsProxyController implements StaticConfigResourceController<SettingsProxyModel>, ValidateController<SettingsProxyModel>, ReadPageController<AlertPagedModel<SettingsProxyModel>> {
    private final SettingsProxyCrudActions configActions;
    private final SettingsProxyValidationAction validationAction;

    @Autowired
    public SettingsProxyController(SettingsProxyCrudActions configActions, SettingsProxyValidationAction validationAction) {
        this.configActions = configActions;
        this.validationAction = validationAction;
    }

    @Override
    public SettingsProxyModel create(SettingsProxyModel resource) {
        return ResponseFactory.createContentResponseFromAction(configActions.create(resource));
    }

    @Override
    public SettingsProxyModel getOne(UUID id) {
        return ResponseFactory.createContentResponseFromAction(configActions.getOne(id));
    }

    @Override
    public AlertPagedModel<SettingsProxyModel> getPage(Integer pageNumber, Integer pageSize, String searchTerm) {
        return ResponseFactory.createContentResponseFromAction(configActions.getPaged(pageNumber, pageSize));
    }

    @Override
    public void update(UUID id, SettingsProxyModel resource) {
        ResponseFactory.createContentResponseFromAction(configActions.update(id, resource));
    }

    @Override
    public void delete(UUID id) {
        ResponseFactory.createContentResponseFromAction(configActions.delete(id));
    }

    @Override
    public ValidationResponseModel validate(SettingsProxyModel requestBody) {
        return ResponseFactory.createContentResponseFromAction(validationAction.validate(requestBody));
    }
}
