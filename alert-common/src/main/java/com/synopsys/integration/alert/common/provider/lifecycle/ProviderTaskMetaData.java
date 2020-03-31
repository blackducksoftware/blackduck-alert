package com.synopsys.integration.alert.common.provider.lifecycle;

import com.synopsys.integration.alert.common.workflow.task.TaskMetaData;

public class ProviderTaskMetaData extends TaskMetaData {
    private static final long serialVersionUID = -445251784769462358L;

    private String providerName;
    private Long configurationId;
    private String configurationName;

    public ProviderTaskMetaData(String taskName, String name, String fullyQualifiedName, String nextRunTime, String providerName, Long configurationId, String configurationName) {
        super(taskName, name, fullyQualifiedName, nextRunTime);
        this.providerName = providerName;
        this.configurationId = configurationId;
        this.configurationName = configurationName;
    }

    public String getProviderName() {
        return providerName;
    }

    public Long getConfigurationId() {
        return configurationId;
    }

    public String getConfigurationName() {
        return configurationName;
    }
}
