package com.synopsys.integration.alert.common.descriptor.config.field.validators;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

public class GlobalConfigExistsValidator implements ConfigValidationFunction {
    public static final String GLOBAL_CONFIG_MISSING = "Configuration missing.";
    private static final Logger logger = LoggerFactory.getLogger(GlobalConfigExistsValidator.class);
    private ConfigurationAccessor configurationAccessor;

    public GlobalConfigExistsValidator(ConfigurationAccessor configurationAccessor) {
        this.configurationAccessor = configurationAccessor;
    }

    @Override
    public Collection<String> apply(FieldValueModel fieldValueModel, FieldModel fieldModel) {
        String descriptorName = fieldValueModel.getValue().orElse("");
        try {
            List<ConfigurationModel> configurations = configurationAccessor.getConfigurationByDescriptorNameAndContext(descriptorName, ConfigContextEnum.GLOBAL);
            if (configurations.isEmpty()) {
                return List.of(GLOBAL_CONFIG_MISSING);
            }
        } catch (AlertDatabaseConstraintException ex) {
            logger.error("Error validating configuration.", ex);
            return List.of(GLOBAL_CONFIG_MISSING);
        }
        return List.of();
    }
}
