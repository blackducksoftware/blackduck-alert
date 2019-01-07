package com.synopsys.integration.alert.provider.polaris.descriptor;

import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;

@Component
public class PolarisGlobalUIConfig extends UIConfig {
    public PolarisGlobalUIConfig() {
        super(PolarisDescriptor.POLARIS_LABEL, PolarisDescriptor.POLARIS_URL, PolarisDescriptor.POLARIS_ICON);
    }

    @Override
    public List<ConfigField> createFields() {
        final ConfigField polarisUrl = TextInputConfigField.createRequired(PolarisDescriptor.POLARIS_URL, "Url");
        // FIXME implement
        return List.of(polarisUrl);
    }
}
