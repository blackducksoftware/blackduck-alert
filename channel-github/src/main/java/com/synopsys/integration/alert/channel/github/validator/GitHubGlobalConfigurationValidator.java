package com.synopsys.integration.alert.channel.github.validator;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatusMessages;
import com.synopsys.integration.alert.channel.github.database.accessor.GitHubGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.github.model.GitHubGlobalConfigModel;

@Component
public class GitHubGlobalConfigurationValidator {
    private final GitHubGlobalConfigAccessor gitHubGlobalConfigAccessor;

    @Autowired
    public GitHubGlobalConfigurationValidator(GitHubGlobalConfigAccessor gitHubGlobalConfigAccessor) {
        this.gitHubGlobalConfigAccessor = gitHubGlobalConfigAccessor;
    }

    public ValidationResponseModel validate(GitHubGlobalConfigModel model, String id) {
        Set<AlertFieldStatus> statuses = new HashSet<>();

        if (StringUtils.isBlank(model.getName())) {
            statuses.add(AlertFieldStatus.error("name", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        } else if (doesNameExist(model.getName(), id)) {
            statuses.add(AlertFieldStatus.error("name", AlertFieldStatusMessages.DUPLICATE_NAME_FOUND));
        }

        if (model.getApiToken().isEmpty() && !model.getIsApiTokenSet().orElse(Boolean.FALSE)) {
            statuses.add(AlertFieldStatus.error("api_token", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }

        if (model.getTimeoutInSeconds() == null ) {
            statuses.add(AlertFieldStatus.error("timeout_seconds", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        } else if (model.getTimeoutInSeconds().compareTo(0L) < 0) {
            statuses.add(AlertFieldStatus.error("timeout_seconds", AlertFieldStatusMessages.INVALID_OPTION));

        }

        if (!statuses.isEmpty()) {
            return ValidationResponseModel.fromStatusCollection(statuses);
        }

        return ValidationResponseModel.success();
    }

    private boolean doesNameExist(String name, @Nullable String currentConfigId) {
        return gitHubGlobalConfigAccessor.getConfigurationByName(name)
            .map(GitHubGlobalConfigModel::getId)
            .filter(id -> (currentConfigId != null) ? !currentConfigId.equals(id) : true)
            .isPresent();
    }
}
