package com.synopsys.integration.alert.provider.polaris.descriptor;

import java.util.Collection;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.workflow.processor.MessageContentCollector;
import com.synopsys.integration.alert.database.api.configuration.model.DefinedFieldModel;
import com.synopsys.integration.alert.provider.polaris.PolarisProvider;

@Component
public class PolarisDescriptor extends ProviderDescriptor {
    public static final String KEY_POLARIS_URL = "polaris.url";

    public static final String POLARIS_ICON = "desktop";
    public static final String POLARIS_LABEL = "Polaris";
    public static final String POLARIS_URL = "polaris";

    @Autowired
    public PolarisDescriptor(final PolarisGlobalDescriptorActionApi polarisGlobalDescriptorActionApi, final PolarisGlobalUIConfig polarisGlobalUIConfig, final PolarisDistributionDescriptorActionApi polarisDistributionDescriptorActionApi,
        final PolarisDistributionUIConfig polarisDistributionUIConfig, final @NotNull PolarisProvider provider) {
        super(polarisGlobalDescriptorActionApi, polarisGlobalUIConfig, polarisDistributionDescriptorActionApi, polarisDistributionUIConfig, provider);
    }

    @Override
    public Set<MessageContentCollector> createTopicCollectors() {
        // FIXME implement topic collectors
        return Set.of();
    }

    @Override
    public Collection<DefinedFieldModel> getDefinedFields(final ConfigContextEnum context) {
        // FIXME configure defined fields
        return Set.of();
    }
}
