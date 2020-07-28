package com.synopsys.integration.alert.channel.azure.boards;

import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueSearchProperties;

public class AzureBoardsSearchProperties implements IssueSearchProperties {
    private final String azureProjectName;
    private final String azureWorkItemType;
    private final String alertKey;

    public AzureBoardsSearchProperties(String azureProjectName, String azureWorkItemType, String alertKey) {
        this.azureProjectName = azureProjectName;
        this.azureWorkItemType = azureWorkItemType;
        this.alertKey = alertKey;
    }

    public String getAzureProjectName() {
        return azureProjectName;
    }

    public String getAzureWorkItemType() {
        return azureWorkItemType;
    }

    public String getAlertKey() {
        return alertKey;
    }

}
