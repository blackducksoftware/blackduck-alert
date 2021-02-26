/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.collector.builder;

public class MessageBuilderConstants {
    public static final String LABEL_COMPONENT_NAME = "Component";
    public static final String LABEL_COMPONENT_VERSION_NAME = "Component Version";
    public static final String LABEL_COMPONENT_LICENSE = "Component License";
    public static final String LABEL_COMPONENT_USAGE = "Component Usage";
    public static final String LABEL_POLICY_NAME = "Policy Violated";
    public static final String LABEL_POLICY_SEVERITY_NAME = "Severity";
    public static final String LABEL_PROJECT_NAME = "Project";
    public static final String LABEL_PROJECT_VERSION_NAME = "Project Version";
    public static final String LABEL_POLICY_OVERRIDE_BY = "Policy Overridden by";
    public static final String LABEL_LICENSE_LIMIT_MESSAGE = "License Limit Message";
    public static final String LABEL_VULNERABILITIES = "Vulnerabilities";
    public static final String LABEL_VULNERABILITY_SEVERITY = "Severity";
    public static final String LABEL_REMEDIATION = "Remediation";
    public static final String REMEDIATION_ERROR_VALUE = "Error retrieving the remediation information";
    public static final String LABEL_REMEDIATION_FIX_PREVIOUS = LABEL_REMEDIATION + " - Fixes Previous Vulnerabilities";
    public static final String LABEL_REMEDIATION_CLEAN = LABEL_REMEDIATION + " - Without Vulnerabilities";
    public static final String LABEL_REMEDIATION_LATEST = LABEL_REMEDIATION + " - Latest Version";
    public static final String LABEL_GUIDANCE = "Guidance";
    public static final String LABEL_GUIDANCE_SHORT_TERM = LABEL_GUIDANCE + " - Short term";
    public static final String LABEL_GUIDANCE_LONG_TERM = LABEL_GUIDANCE + " - Long term";
    public static final String VULNERABILITY_CHECK_TEXT = "vuln";
    public static final String CATEGORY_TYPE_POLICY = "Policy";
    public static final String CATEGORY_TYPE_VULNERABILITY = "Vulnerability";
    public static final String LABEL_USAGE_INFO = "Usage Info";

    private MessageBuilderConstants() {}
}
