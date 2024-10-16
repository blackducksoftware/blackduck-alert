/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.processor.model;

import com.blackduck.integration.blackduck.api.manual.component.PolicyInfo;

public class PolicyOverrideUniquePolicyNotificationContent extends AbstractProjectVersionNotificationContent {
    private final String componentName;
    private final String componentVersionName;
    private final String firstName;
    private final String lastName;
    private final PolicyInfo policyInfo;
    private final String policy;
    private final String bomComponentVersionPolicyStatus;
    private final String bomComponent;

    public PolicyOverrideUniquePolicyNotificationContent(
        String projectName,
        String projectVersionName,
        String projectVersion,
        String componentName,
        String componentVersionName,
        String firstName,
        String lastName,
        PolicyInfo policyInfo,
        String policy,
        String bomComponentVersionPolicyStatus,
        String bomComponent
    ) {
        super(projectName, projectVersionName, projectVersion);
        this.componentName = componentName;
        this.componentVersionName = componentVersionName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.policyInfo = policyInfo;
        this.policy = policy;
        this.bomComponentVersionPolicyStatus = bomComponentVersionPolicyStatus;
        this.bomComponent = bomComponent;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getComponentVersionName() {
        return componentVersionName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public PolicyInfo getPolicyInfo() {
        return policyInfo;
    }

    public String getPolicy() {
        return policy;
    }

    public String getBomComponentVersionPolicyStatus() {
        return bomComponentVersionPolicyStatus;
    }

    public String getBomComponent() {
        return bomComponent;
    }

}
