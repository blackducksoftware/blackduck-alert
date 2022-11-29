package com.synopsys.integration.alert.channel.azure.boards.validator;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatusMessages;
import com.synopsys.integration.alert.channel.azure.boards.database.accessor.AzureBoardsGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.synopsys.integration.alert.channel.azure.boards.oauth.OAuthRequestValidator;

@Component
public class AzureBoardsGlobalConfigurationValidator {
    private static final String AUTHENTICATION_IN_PROGRESS = "Authentication in progress cannot perform current action.";
    private final AzureBoardsGlobalConfigAccessor azureBoardsGlobalConfigAccessor;
    private final OAuthRequestValidator oAuthRequestValidator;

    @Autowired
    public AzureBoardsGlobalConfigurationValidator(AzureBoardsGlobalConfigAccessor azureBoardsGlobalConfigAccessor, OAuthRequestValidator oAuthRequestValidator) {
        this.azureBoardsGlobalConfigAccessor = azureBoardsGlobalConfigAccessor;
        this.oAuthRequestValidator = oAuthRequestValidator;
    }

    public ValidationResponseModel validate(AzureBoardsGlobalConfigModel model, String id) {
        Set<AlertFieldStatus> statuses = new HashSet<>();

        if (model.getId() != null && oAuthRequestValidator.hasRequestFromConfigurationId(model.getId())) {
            statuses.add(AlertFieldStatus.error("oAuth", AUTHENTICATION_IN_PROGRESS));
        }

        if (StringUtils.isBlank(model.getName())) {
            statuses.add(AlertFieldStatus.error("name", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        } else if (doesNameExist(model.getName(), id)) {
            statuses.add(AlertFieldStatus.error("name", AlertFieldStatusMessages.DUPLICATE_NAME_FOUND));
        }
        if (StringUtils.isBlank(model.getOrganizationName())) {
            statuses.add(AlertFieldStatus.error("organizationName", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }
        if (model.getAppId().isEmpty() && !model.getIsAppIdSet().orElse(Boolean.FALSE)) {
            statuses.add(AlertFieldStatus.error("appId", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }
        if (model.getClientSecret().isEmpty() && !model.getIsClientSecretSet().orElse(Boolean.FALSE)) {
            statuses.add(AlertFieldStatus.error("clientSecret", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }

        if (!statuses.isEmpty()) {
            return ValidationResponseModel.fromStatusCollection(statuses);
        }
        return ValidationResponseModel.success();
    }

    //Checks if a configuration already exists then checks if we're updating (currentConfigId == id) the found configuration
    private boolean doesNameExist(String name, @Nullable String currentConfigId) {
        return azureBoardsGlobalConfigAccessor.getConfigurationByName(name)
            .map(AzureBoardsGlobalConfigModel::getId)
            .filter(id -> currentConfigId == null || !currentConfigId.equals(id))
            .isPresent();
    }
}
