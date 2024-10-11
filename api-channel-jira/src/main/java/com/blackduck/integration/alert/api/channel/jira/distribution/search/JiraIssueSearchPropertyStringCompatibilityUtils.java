/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.jira.distribution.search;

import org.apache.commons.lang3.StringUtils;

import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcernType;

public class JiraIssueSearchPropertyStringCompatibilityUtils {
    public static String createCategory(ComponentConcernType concernType) {
        return StringUtils.capitalize(concernType.name().toLowerCase());
    }

    public static String createPolicyAdditionalKey(String policyName) {
        return String.format("Policy Violated%s", policyName);
    }

    private JiraIssueSearchPropertyStringCompatibilityUtils() {
    }

}
