package com.synopsys.integration.alert.common.action;

import java.util.HashMap;
import java.util.Map;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;

public abstract class ConfigurationAction {
    private final String descriptorName;
    private final Map<ConfigContextEnum, ApiAction> apiActionMap = new HashMap<>();
    private final Map<ConfigContextEnum, TestAction> testActionMap = new HashMap<>();

    protected ConfigurationAction(final String descriptorName) {
        this.descriptorName = descriptorName;
    }

    public String getDescriptorName() {
        return descriptorName;
    }

    public void addGlobalApiAction(final ApiAction apiAction) {
        apiActionMap.put(ConfigContextEnum.GLOBAL, apiAction);
    }

    public void addDistributionApiAction(final ApiAction apiAction) {
        apiActionMap.put(ConfigContextEnum.DISTRIBUTION, apiAction);
    }

    public void addGlobalTestAction(final TestAction testAction) {
        testActionMap.put(ConfigContextEnum.GLOBAL, testAction);
    }

    public void addDistributionTestAction(final TestAction testAction) {
        testActionMap.put(ConfigContextEnum.DISTRIBUTION, testAction);
    }

    public ApiAction getApiAction(final ConfigContextEnum context) {
        return apiActionMap.get(context);
    }

    public TestAction getTestAction(final ConfigContextEnum context) {
        return testActionMap.get(context);
    }

    public void runTestAction(final ConfigContextEnum context, final TestConfigModel testConfigModel) throws IntegrationException {
        final TestAction testAction = getTestAction(context);
        testAction.testConfig(testConfigModel);
    }

}
