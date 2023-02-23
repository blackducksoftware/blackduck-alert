package com.synopsys.integration.alert.channel.azure.boards.environment;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.AlertConstants;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.channel.azure.boards.database.accessor.AzureBoardsGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.synopsys.integration.alert.channel.azure.boards.validator.AzureBoardsGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.environment.EnvironmentProcessingResult;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandler;
import com.synopsys.integration.alert.environment.EnvironmentVariableUtility;

@Component
public class AzureBoardsEnvironmentVariableHandler extends EnvironmentVariableHandler<AzureBoardsGlobalConfigModel>  {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String APP_ID_KEY = "ALERT_CHANNEL_AZURE_BOARDS_AZURE_BOARDS_APP_ID";
    public static final String CLIENT_SECRET_KEY = "ALERT_CHANNEL_AZURE_BOARDS_AZURE_BOARDS_CLIENT_SECRET";
    public static final String ORGANIZATION_NAME_KEY = "ALERT_CHANNEL_AZURE_BOARDS_AZURE_BOARDS_ORGANIZATION_NAME";

    public static final Set<String> VARIABLE_NAMES = Set.of(APP_ID_KEY, CLIENT_SECRET_KEY, ORGANIZATION_NAME_KEY);

    private final AzureBoardsGlobalConfigAccessor configAccessor;
    private final EnvironmentVariableUtility environmentVariableUtility;
    private final AzureBoardsGlobalConfigurationValidator validator;

    @Autowired
    public AzureBoardsEnvironmentVariableHandler(AzureBoardsGlobalConfigAccessor configAccessor, EnvironmentVariableUtility environmentVariableUtility, AzureBoardsGlobalConfigurationValidator validator) {
        super(ChannelKeys.AZURE_BOARDS.getDisplayName(), VARIABLE_NAMES, environmentVariableUtility);
        this.configAccessor = configAccessor;
        this.environmentVariableUtility = environmentVariableUtility;
        this.validator = validator;
    }

    @Override
    protected Boolean configurationMissingCheck() {
        return configAccessor.getConfigurationCount() <= 0;
    }

    @Override
    protected AzureBoardsGlobalConfigModel configureModel() {
        String name = AlertRestConstants.DEFAULT_CONFIGURATION_NAME;
        String appId = environmentVariableUtility.getEnvironmentValue(APP_ID_KEY).orElse(null);
        String clientSecret = environmentVariableUtility.getEnvironmentValue(CLIENT_SECRET_KEY).orElse(null);
        String organizationName = environmentVariableUtility.getEnvironmentValue(ORGANIZATION_NAME_KEY).orElse(null);
        String createdAt = DateUtils.formatDate(DateUtils.createCurrentDateTimestamp(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        AzureBoardsGlobalConfigModel configModel = new AzureBoardsGlobalConfigModel(null, name, organizationName, appId, clientSecret);
        configModel.setCreatedAt(createdAt);
        configModel.setLastUpdated(createdAt);

        return configModel;
    }

    @Override
    protected ValidationResponseModel validateConfiguration(AzureBoardsGlobalConfigModel configModel) {
        return validator.validate(configModel, null);
    }

    @Override
    protected EnvironmentProcessingResult buildProcessingResult(AzureBoardsGlobalConfigModel obfuscatedConfigModel) {
        EnvironmentProcessingResult.Builder builder = new EnvironmentProcessingResult.Builder(VARIABLE_NAMES);

        if (StringUtils.isNotBlank(obfuscatedConfigModel.getOrganizationName())) {
            builder.addVariableValue(ORGANIZATION_NAME_KEY, obfuscatedConfigModel.getOrganizationName());
        }

        obfuscatedConfigModel.getIsAppIdSet()
            .filter(Boolean::booleanValue)
            .ifPresent(ignored -> builder.addVariableValue(APP_ID_KEY, AlertConstants.MASKED_VALUE));

        obfuscatedConfigModel.getIsClientSecretSet()
            .filter(Boolean::booleanValue)
            .ifPresent(ignored -> builder.addVariableValue(CLIENT_SECRET_KEY, AlertConstants.MASKED_VALUE));

        return builder.build();
    }

    @Override
    protected void saveConfiguration(AzureBoardsGlobalConfigModel configModel, EnvironmentProcessingResult processingResult) {
        if (configAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME).isEmpty()) {
            try {
                configAccessor.createConfiguration(configModel);
            } catch (AlertConfigurationException ex) {
                logger.error("Failed to create config: ", ex);
            }
        }
    }
}
