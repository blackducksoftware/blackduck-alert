package com.synopsys.integration.alert.authentication.saml.environment;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.authentication.saml.database.accessor.SAMLConfigAccessor;
import com.synopsys.integration.alert.authentication.saml.model.SAMLConfigModel;
import com.synopsys.integration.alert.authentication.saml.validator.SAMLConfigurationValidator;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.environment.EnvironmentProcessingResult;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandler;
import com.synopsys.integration.alert.environment.EnvironmentVariableUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class SAMLEnvironmentVariableHandler extends EnvironmentVariableHandler<SAMLConfigModel> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String HANDLER_NAME = "SAML Settings";
    public static final String ENVIRONMENT_VARIABLE_PREFIX = "ALERT_AUTHENTICATION_SAML_"; //TODO: verify

    private final SAMLConfigAccessor configAccessor;
    private final SAMLConfigurationValidator validator;
    private final EnvironmentVariableUtility environmentVariableUtility;

    @Autowired
    public SAMLEnvironmentVariableHandler(
        SAMLConfigAccessor configAccessor,
        SAMLConfigurationValidator validator,
        EnvironmentVariableUtility environmentVariableUtility
    ) {
        super(HANDLER_NAME, Set.of());  // TODO: figure out
        this.configAccessor = configAccessor;
        this.validator = validator;
        this.environmentVariableUtility = environmentVariableUtility;
    }

    @Override
    protected Boolean configurationMissingCheck() {
        return !configAccessor.doesConfigurationExist();
    }

    @Override
    protected SAMLConfigModel configureModel() {
        String createdAt = DateUtils.formatDate(DateUtils.createCurrentDateTimestamp(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        SAMLConfigModel samlConfigModel = new SAMLConfigModel();
        samlConfigModel.setCreatedAt(createdAt);
        samlConfigModel.setLastUpdated(createdAt);

        return samlConfigModel;
    }

    @Override
    protected ValidationResponseModel validateConfiguration(SAMLConfigModel configModel) {
        return validator.validate(configModel);
    }

    @Override
    protected EnvironmentProcessingResult buildProcessingResult(SAMLConfigModel obfuscatedConfigModel) {
        EnvironmentProcessingResult.Builder builder = new EnvironmentProcessingResult.Builder();

        // TODO: figure env variables to set

        return builder.build();
    }

    @Override
    protected void saveConfiguration(SAMLConfigModel configModel, EnvironmentProcessingResult processingResult) {
        try {
            configAccessor.createConfiguration(configModel);
        } catch (AlertConfigurationException ex) {
            logger.error("Error creating the configuration: {}", ex.getMessage());
        }
    }
}
