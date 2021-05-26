/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.common.descriptor.config.ui.DescriptorMetadata;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.util.Stringable;

/**
 * Note when creating a new descriptor you will need to register descriptors in the database.
 * <br/>
 * <br/>
 * Use the REGISTER_DESCRIPTOR stored procedure.
 * <br/>
 * The REGISTER_DESCRIPTOR stored procedure will assign default permissions to the descriptor for the well known roles.
 * <br/>
 * The default permissions should be sufficient for channels and providers.
 * <br/>
 * <br/>
 * For components you may need to remove permissions.  In order to do that; use the stored procedures:
 * <br/>
 * <br/>
 * REMOVE_PERMISSION - removes a single permission from the user role for the descriptor and context.
 * <br/>
 * <br/>
 * REMOVE_ALL_PERMISSIONS - removes all permissions from the user role for the descriptor and context.
 */
public abstract class Descriptor extends Stringable {
    private final DescriptorKey descriptorKey;
    private final DescriptorType type;
    private final Map<ConfigContextEnum, UIConfig> uiConfigs;

    public Descriptor(DescriptorKey descriptorKey, DescriptorType type) {
        this.descriptorKey = descriptorKey;
        this.type = type;
        uiConfigs = new EnumMap<>(ConfigContextEnum.class);
    }

    public DescriptorKey getDescriptorKey() {
        return descriptorKey;
    }

    public DescriptorType getType() {
        return type;
    }

    public void addGlobalUiConfig(UIConfig uiConfig) {
        uiConfigs.put(ConfigContextEnum.GLOBAL, uiConfig);
    }

    public void addDistributionUiConfig(UIConfig uiConfig) {
        uiConfigs.put(ConfigContextEnum.DISTRIBUTION, uiConfig);
    }

    public Optional<UIConfig> getUIConfig(ConfigContextEnum actionApiType) {
        return Optional.ofNullable(uiConfigs.get(actionApiType));
    }

    public Optional<DescriptorMetadata> createMetaData(ConfigContextEnum context) {
        return getUIConfig(context).map(uiConfig -> new DescriptorMetadata(descriptorKey, getType(), context));
    }

    public Set<DefinedFieldModel> getAllDefinedFields(ConfigContextEnum context) {
        return getUIConfig(context)
                   .map(UIConfig::getFields)
                   .orElse(List.of())
                   .stream()
                   .map(configField -> new DefinedFieldModel(configField.getKey(), context, configField.isSensitive()))
                   .collect(Collectors.toSet());
    }

    public boolean hasUIConfigs() {
        return uiConfigs.size() > 0;
    }

    public boolean hasUIConfigForType(ConfigContextEnum actionApiType) {
        return uiConfigs.containsKey(actionApiType);
    }

}
