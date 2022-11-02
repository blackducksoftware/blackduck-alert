package com.synopsys.integration.alert.channel.github.web;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.channel.github.action.GitHubGlobalCrudActions;
import com.synopsys.integration.alert.channel.github.model.GitHubGlobalConfigModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.api.ReadPageController;
import com.synopsys.integration.alert.common.rest.api.StaticConfigResourceController;
import com.synopsys.integration.alert.common.rest.api.ValidateController;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;

@RestController
@RequestMapping(AlertRestConstants.GITHUB_CONFIGURATION_PATH)
public class GitHubGlobalConfigController implements StaticConfigResourceController<GitHubGlobalConfigModel>,
    ValidateController<GitHubGlobalConfigModel>, ReadPageController<AlertPagedModel<GitHubGlobalConfigModel>> {
    private final GitHubGlobalCrudActions configActions;

    @Autowired
    public GitHubGlobalConfigController(final GitHubGlobalCrudActions configActions) {
        this.configActions = configActions;
    }

    @Override
    public AlertPagedModel<GitHubGlobalConfigModel> getPage(
        final Integer pageNumber,
        final Integer pageSize,
        final String searchTerm,
        final String sortName,
        final String sortOrder
    ) {
        return ResponseFactory.createContentResponseFromAction(configActions.getPaged(pageNumber, pageSize, searchTerm, sortName, sortOrder));
    }

    @Override
    public GitHubGlobalConfigModel create(final GitHubGlobalConfigModel resource) {
        return ResponseFactory.createContentResponseFromAction(configActions.create(resource));
    }

    @Override
    public GitHubGlobalConfigModel getOne(final UUID id) {
        return ResponseFactory.createContentResponseFromAction(configActions.getOne(id));
    }

    @Override
    public void update(final UUID id, final GitHubGlobalConfigModel resource) {
        ResponseFactory.createContentResponseFromAction(configActions.update(id, resource));
    }

    @Override
    public void delete(final UUID id) {
        ResponseFactory.createContentResponseFromAction(configActions.delete(id));
    }

    @Override
    public ValidationResponseModel validate(final GitHubGlobalConfigModel requestBody) {
        return ValidationResponseModel.success();
    }
}
