package com.synopsys.integration.alert.provider.polaris.descriptor;

import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;

@Component
public class PolarisDistributionUIConfig extends UIConfig {
    public PolarisDistributionUIConfig() {
        super(PolarisDescriptor.POLARIS_LABEL, PolarisDescriptor.POLARIS_URL, PolarisDescriptor.POLARIS_ICON);
    }

    @Override
    public List<ConfigField> createFields() {
        // FIXME implement
        return List.of();
    }
}
