/*
 * api-channel-jira
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.jira.distribution.search;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernType;

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
