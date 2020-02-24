package com.synopsys.integration.alert.common.descriptor.config.ui;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.action.CustomEndpointManager;
import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOption;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.SelectCustomEndpoint;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.model.FieldModel;

@Component
public class ProviderConfigSelectCustomEndpoint extends SelectCustomEndpoint {
    private final ConfigurationAccessor configurationAccessor;
    private final DescriptorMap descriptorMap;

    @Autowired
    public ProviderConfigSelectCustomEndpoint(CustomEndpointManager customEndpointManager, ResponseFactory responseFactory, Gson gson, ConfigurationAccessor configurationAccessor, DescriptorMap descriptorMap) throws AlertException {
        super(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME, customEndpointManager, responseFactory, gson);
        this.configurationAccessor = configurationAccessor;
        this.descriptorMap = descriptorMap;
    }

    @Override
    protected List<LabelValueSelectOption> createData(FieldModel fieldModel) throws AlertException {
        Optional<String> providerName = fieldModel.getFieldValue(ChannelDistributionUIConfig.KEY_PROVIDER_NAME);
        Optional<DescriptorKey> descriptorKey = descriptorMap.getDescriptorKey(providerName.orElse(StringUtils.EMPTY));
        if (descriptorKey.isPresent()) {
            List<LabelValueSelectOption> options = new LinkedList<>();
            List<ConfigurationModel> configurationModels = configurationAccessor.getConfigurationByDescriptorKeyAndContext(descriptorKey.get(), ConfigContextEnum.GLOBAL);
            for (ConfigurationModel configurationModel : configurationModels) {
                FieldAccessor accessor = new FieldAccessor(configurationModel.getCopyOfKeyToFieldMap());
                Optional<String> configName = accessor.getString(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
                if (configName.isPresent()) {
                    options.add(new LabelValueSelectOption(configName.get()));
                }
            }
            return options;
        }
        return List.of();
    }
}
