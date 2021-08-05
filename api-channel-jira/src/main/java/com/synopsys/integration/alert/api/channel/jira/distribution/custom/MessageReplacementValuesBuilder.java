package com.synopsys.integration.alert.api.channel.jira.distribution.custom;

public class MessageReplacementValuesBuilder {
    private String providerName;
    private String projectName;
    private String projectVersionName;
    private String componentName;
    private String componentVersionName;
    private String severity;
    private String policyCategory;

    public static MessageReplacementValues trivial(String providerLabel, String projectName) {
        return new MessageReplacementValuesBuilder(providerLabel, projectName)
                   .projectVersionName(MessageReplacementValues.DEFAULT_NOTIFICATION_REPLACEMENT_VALUE)
                   .componentName(MessageReplacementValues.DEFAULT_NOTIFICATION_REPLACEMENT_VALUE)
                   .componentVersionName(MessageReplacementValues.DEFAULT_NOTIFICATION_REPLACEMENT_VALUE)
                   .severity(MessageReplacementValues.DEFAULT_NOTIFICATION_REPLACEMENT_VALUE)
                   .policyCategory(MessageReplacementValues.DEFAULT_NOTIFICATION_REPLACEMENT_VALUE)
                   .build();
    }

    public MessageReplacementValuesBuilder(String providerName, String projectName) {
        this.providerName = providerName;
        this.projectName = projectName;
    }

    public MessageReplacementValues build() {
        return new MessageReplacementValues(
            providerName,
            projectName,
            projectVersionName,
            componentName,
            componentVersionName,
            severity,
            policyCategory
        );
    }

    public MessageReplacementValuesBuilder providerName(String providerName) {
        this.providerName = providerName;
        return this;
    }

    public MessageReplacementValuesBuilder projectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public MessageReplacementValuesBuilder projectVersionName(String projectVersionName) {
        this.projectVersionName = projectVersionName;
        return this;
    }

    public MessageReplacementValuesBuilder componentName(String componentName) {
        this.componentName = componentName;
        return this;
    }

    public MessageReplacementValuesBuilder componentVersionName(String componentVersionName) {
        this.componentVersionName = componentVersionName;
        return this;
    }

    public MessageReplacementValuesBuilder severity(String severity) {
        this.severity = severity;
        return this;
    }

    public MessageReplacementValuesBuilder policyCategory(String policyCategory) {
        this.policyCategory = policyCategory;
        return this;
    }
}
