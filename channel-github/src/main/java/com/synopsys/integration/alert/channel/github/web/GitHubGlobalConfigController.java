package com.synopsys.integration.alert.channel.github.web;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.channel.github.action.GitHubGlobalCrudActions;
import com.synopsys.integration.alert.channel.github.action.GitHubGlobalTestAction;
import com.synopsys.integration.alert.channel.github.action.GitHubGlobalValidationAction;
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
    private final GitHubGlobalValidationAction gitHubGlobalValidationAction;
    private final GitHubGlobalCrudActions configActions;
    private final GitHubGlobalTestAction testAction;

    @Autowired
    public GitHubGlobalConfigController(GitHubGlobalValidationAction gitHubGlobalValidationAction, GitHubGlobalCrudActions configActions, GitHubGlobalTestAction testAction) {
        this.gitHubGlobalValidationAction = gitHubGlobalValidationAction;
        this.configActions = configActions;
        this.testAction = testAction;
    }

    @Override
    public AlertPagedModel<GitHubGlobalConfigModel> getPage(
        Integer pageNumber,
        Integer pageSize,
        String searchTerm,
        String sortName,
        String sortOrder
    ) {
        return ResponseFactory.createContentResponseFromAction(configActions.getPaged(pageNumber, pageSize, searchTerm, sortName, sortOrder));
    }

    @Override
    public GitHubGlobalConfigModel create(GitHubGlobalConfigModel resource) {
        return ResponseFactory.createContentResponseFromAction(configActions.create(resource));
    }

    @Override
    public GitHubGlobalConfigModel getOne(UUID id) {
        return ResponseFactory.createContentResponseFromAction(configActions.getOne(id));
    }

    @Override
    public void update(UUID id, GitHubGlobalConfigModel resource) {
        ResponseFactory.createContentResponseFromAction(configActions.update(id, resource));
    }

    @Override
    public void delete(UUID id) {
        ResponseFactory.createContentResponseFromAction(configActions.delete(id));
    }

    @PostMapping("/test")
    public ValidationResponseModel test(@RequestBody GitHubGlobalConfigModel resource) {
        return ResponseFactory.createContentResponseFromAction(testAction.testWithPermissionCheck(resource));
    }

    @Override
    public ValidationResponseModel validate(GitHubGlobalConfigModel requestBody) {
        return ResponseFactory.createContentResponseFromAction(gitHubGlobalValidationAction.validate(requestBody));
    }
}
