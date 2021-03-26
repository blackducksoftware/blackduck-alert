/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.action;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.job.details.processor.DistributionJobDetailsExtractor;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public abstract class ConfigurationAction {
    private final DescriptorKey descriptorKey;
    private final Map<ConfigContextEnum, ApiAction> apiActionMap = new EnumMap<>(ConfigContextEnum.class);
    private final Map<ConfigContextEnum, TestAction> testActionMap = new EnumMap<>(ConfigContextEnum.class);

    // TODO This Should probably receive the same fix as the channelDistributionTestAction object as well.
    private DistributionJobDetailsExtractor distributionJobModelExtractor;

    protected ConfigurationAction(DescriptorKey descriptorKey) {
        this.descriptorKey = descriptorKey;
    }

    public DescriptorKey getDescriptorKey() {
        return descriptorKey;
    }

    public void addGlobalApiAction(ApiAction apiAction) {
        apiActionMap.put(ConfigContextEnum.GLOBAL, apiAction);
    }

    public void addDistributionApiAction(ApiAction apiAction) {
        apiActionMap.put(ConfigContextEnum.DISTRIBUTION, apiAction);
    }

    public void addGlobalTestAction(TestAction testAction) {
        testActionMap.put(ConfigContextEnum.GLOBAL, testAction);
    }

    public void addDistributionTestAction(TestAction testAction) {
        testActionMap.put(ConfigContextEnum.DISTRIBUTION, testAction);
    }

    public ApiAction getApiAction(ConfigContextEnum context) {
        return apiActionMap.get(context);
    }

    public TestAction getTestAction(ConfigContextEnum context) {
        return testActionMap.get(context);
    }

    public void addJobDetailsExtractor(DistributionJobDetailsExtractor distributionJobModelExtractor) {
        this.distributionJobModelExtractor = distributionJobModelExtractor;
    }

    public Optional<DistributionJobDetailsExtractor> getJobDetailsExtractor() {
        return Optional.ofNullable(distributionJobModelExtractor);
    }

}
