package com.synopsys.integration.alert.provider.blackduck.action;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ApiAction;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.provider.state.StatefulProvider;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.action.delegator.BlackDuckAddProjectUserDelegator;

@Component
public class BlackDuckDistributionApiAction extends ApiAction {
    private final ConfigurationAccessor configurationAccessor;
    private final BlackDuckProvider blackDuckProvider;
    private final BlackDuckAddProjectUserDelegator blackDuckAddProjectUserDelegator;

    @Autowired
    public BlackDuckDistributionApiAction(ConfigurationAccessor configurationAccessor, BlackDuckProvider blackDuckProvider, BlackDuckAddProjectUserDelegator blackDuckAddProjectUserDelegator) {
        this.configurationAccessor = configurationAccessor;
        this.blackDuckProvider = blackDuckProvider;
        this.blackDuckAddProjectUserDelegator = blackDuckAddProjectUserDelegator;
    }

    @Override
    public FieldModel afterSaveAction(FieldModel fieldModel) throws AlertException {
        afterWrite(fieldModel);
        return super.afterSaveAction(fieldModel);
    }

    @Override
    public FieldModel afterUpdateAction(FieldModel previousFieldModel, FieldModel currentFieldModel) throws AlertException {
        afterWrite(currentFieldModel);
        return super.afterUpdateAction(previousFieldModel, currentFieldModel);
    }

    private void afterWrite(FieldModel currentFieldModel) throws AlertException {
        Optional<Long> optionalProviderConfigId = currentFieldModel.getFieldValue(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID).map(Long::valueOf);
        if (optionalProviderConfigId.isPresent()) {
            Optional<ConfigurationModel> optionalBlackDuckGlobalConfig = configurationAccessor.getConfigurationById(optionalProviderConfigId.get());
            if (optionalBlackDuckGlobalConfig.isPresent()) {
                StatefulProvider statefulProvider = blackDuckProvider.createStatefulProvider(optionalBlackDuckGlobalConfig.get());
                BlackDuckProperties properties = (BlackDuckProperties) statefulProvider.getProperties();
                Collection<String> configuredProjects = currentFieldModel.getFieldValueModel(ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT)
                                                            .map(FieldValueModel::getValues)
                                                            .orElse(Set.of());
                blackDuckAddProjectUserDelegator.addProviderUserToBlackDuckProjects(properties, configuredProjects);
            }
        }
    }

}
