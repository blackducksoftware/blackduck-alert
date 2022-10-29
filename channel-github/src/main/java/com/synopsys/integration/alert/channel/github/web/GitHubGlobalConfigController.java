package com.synopsys.integration.alert.channel.github.web;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.channel.github.model.GitHubConfigModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.api.ReadPageController;
import com.synopsys.integration.alert.common.rest.api.StaticConfigResourceController;
import com.synopsys.integration.alert.common.rest.api.ValidateController;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.util.DateUtils;

@RestController
@RequestMapping(AlertRestConstants.GITHUB_CONFIGURATION_PATH)
public class GitHubGlobalConfigController implements StaticConfigResourceController<GitHubConfigModel>,
    ValidateController<GitHubConfigModel>, ReadPageController<AlertPagedModel<GitHubConfigModel>> {

    //TODO Implement the action classes and use them here.
    @Override
    public AlertPagedModel<GitHubConfigModel> getPage(final Integer pageNumber, final Integer pageSize, final String searchTerm, final String sortName, final String sortOrder) {
        return new AlertPagedModel<>(1, 1, 1, List.of(getOne(UUID.randomUUID())));
    }

    @Override
    public GitHubConfigModel create(final GitHubConfigModel resource) {
        String currentDate = DateUtils.createCurrentDateAsJsonString();
        return new GitHubConfigModel(
            UUID.randomUUID().toString(),
            resource.getName(),
            currentDate,
            currentDate,
            resource.getApiToken(),
            StringUtils.isNotBlank(resource.getApiToken()),
            resource.getTimeoutInSeconds()
        );
    }

    @Override
    public GitHubConfigModel getOne(final UUID id) {
        String currentDate = DateUtils.createCurrentDateAsJsonString();
        return new GitHubConfigModel(
            UUID.randomUUID().toString(),
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            currentDate,
            currentDate,
            null,
            true,
            500L
        );
    }

    @Override
    public void update(final UUID id, final GitHubConfigModel resource) {

    }

    @Override
    public void delete(final UUID id) {

    }

    @Override
    public ValidationResponseModel validate(final GitHubConfigModel requestBody) {
        return ValidationResponseModel.success();
    }
}
