package com.synopsys.integration.alert.common.descriptor.config.ui;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Streams;
import com.synopsys.integration.alert.common.descriptor.config.field.CheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.provider.ProviderKey;

public abstract class ProviderGlobalUIConfig extends UIConfig {
    public static final String KEY_COMMON_PROVIDER_PREFIX = "provider.common.";
    public static final String KEY_PROVIDER_CONFIG_ENABLED = KEY_COMMON_PROVIDER_PREFIX + "config.enabled";
    public static final String KEY_PROVIDER_CONFIG_NAME = KEY_COMMON_PROVIDER_PREFIX + "config.name";

    public static final String LABEL_PROVIDER_CONFIG_ENABLED = "Enabled";
    public static final String LABEL_PROVIDER_CONFIG_NAME = "Configuration Name";

    public static final String DESCRIPTION_PROVIDER_CONFIG_ENABLED =
        "If selected, this provider configuration will be able to pull data into Alert and be available to configure with distribution jobs, otherwise, it will not be available for those usages.";
    public static final String DESCRIPTION_PROVIDER_CONFIG_NAME = "The name of this provider configuration. Must be unique.";

    private final ProviderKey providerKey;

    public ProviderGlobalUIConfig(ProviderKey providerKey, String label, String description, String urlName) {
        super(label, description, urlName);
        this.providerKey = providerKey;
    }

    @Override
    public final List<ConfigField> createFields() {
        ConfigField providerConfigEnabled = new CheckboxConfigField(KEY_PROVIDER_CONFIG_ENABLED, LABEL_PROVIDER_CONFIG_ENABLED, DESCRIPTION_PROVIDER_CONFIG_ENABLED).applyDefaultValue(Boolean.TRUE.toString());
        ConfigField providerConfigName = new TextInputConfigField(KEY_PROVIDER_CONFIG_NAME, LABEL_PROVIDER_CONFIG_NAME, DESCRIPTION_PROVIDER_CONFIG_NAME).applyRequired(true);

        List<ConfigField> providerCommonGlobalFields = List.of(providerConfigName, providerConfigEnabled);
        List<ConfigField> providerGlobalFields = createProviderGlobalFields();
        return Streams.concat(providerCommonGlobalFields.stream(), providerGlobalFields.stream()).collect(Collectors.toList());
    }

    public abstract List<ConfigField> createProviderGlobalFields();

    public ProviderKey getProviderKey() {
        return providerKey;
    }

}
