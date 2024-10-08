package com.blackduck.integration.alert.channel.azure.boards.convert;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.blackduck.integration.alert.channel.azure.boards.validator.AzureBoardsGlobalConfigurationValidator;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.action.api.GlobalConfigurationModelToConcreteConverter;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;

/**
 * @deprecated This class is used to support conversion between FieldModels and GlobalConfigurationModels. When FieldModels are deprecated in 8.0.0
 * this class will no longer be necessary.
 */
@Component
@Deprecated(forRemoval = true)
public class AzureBoardsGlobalConfigurationModelConverter extends GlobalConfigurationModelToConcreteConverter<AzureBoardsGlobalConfigModel> {
    public static final String ORGANIZATION_NAME = "azure.boards.organization.name";
    public static final String CLIENT_ID = "azure.boards.client.id";
    public static final String CLIENT_SECRET = "azure.boards.client.secret";
    private final AzureBoardsGlobalConfigurationValidator validator;

    @Autowired
    public AzureBoardsGlobalConfigurationModelConverter(AzureBoardsGlobalConfigurationValidator validator) {
        this.validator = validator;
    }

    @Override
    protected Optional<AzureBoardsGlobalConfigModel> convert(ConfigurationModel globalConfigurationModel) {
        String organizationName = globalConfigurationModel.getField(ORGANIZATION_NAME)
            .flatMap(ConfigurationFieldModel::getFieldValue)
            .orElse(null);
        if (StringUtils.isBlank(organizationName)) {
            return Optional.empty();
        }

        String clientId = globalConfigurationModel.getField(CLIENT_ID)
            .flatMap(ConfigurationFieldModel::getFieldValue)
            .orElse(null);

        String clientSecret = globalConfigurationModel.getField(CLIENT_SECRET)
            .flatMap(ConfigurationFieldModel::getFieldValue)
            .orElse(null);

        AzureBoardsGlobalConfigModel model = new AzureBoardsGlobalConfigModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, organizationName, clientId, clientSecret);

        return Optional.of(model);
    }

    @Override
    protected ValidationResponseModel validate(AzureBoardsGlobalConfigModel configModel, String existingConfigurationId) {
        return validator.validate(configModel, existingConfigurationId);
    }
}
