package com.blackduck.integration.alert.common.action;

import java.util.EnumMap;
import java.util.Map;

import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;

@Deprecated(forRemoval = true)
public abstract class ConfigurationAction {
    private final DescriptorKey descriptorKey;
    private final Map<ConfigContextEnum, ApiAction> apiActionMap = new EnumMap<>(ConfigContextEnum.class);
    private final Map<ConfigContextEnum, FieldModelTestAction> testActionMap = new EnumMap<>(ConfigContextEnum.class);

    protected ConfigurationAction(DescriptorKey descriptorKey) {
        this.descriptorKey = descriptorKey;
    }

    public DescriptorKey getDescriptorKey() {
        return descriptorKey;
    }

    public void addGlobalApiAction(ApiAction apiAction) {
        apiActionMap.put(ConfigContextEnum.GLOBAL, apiAction);
    }

    public void addGlobalTestAction(FieldModelTestAction fieldModelTestAction) {
        testActionMap.put(ConfigContextEnum.GLOBAL, fieldModelTestAction);
    }

    public void addDistributionTestAction(FieldModelTestAction fieldModelTestAction) {
        testActionMap.put(ConfigContextEnum.DISTRIBUTION, fieldModelTestAction);
    }

    public ApiAction getApiAction(ConfigContextEnum context) {
        return apiActionMap.get(context);
    }

    public FieldModelTestAction getTestAction(ConfigContextEnum context) {
        return testActionMap.get(context);
    }

}
