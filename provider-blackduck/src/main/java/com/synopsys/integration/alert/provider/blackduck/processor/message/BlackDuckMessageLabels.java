/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.processor.message;

public final class BlackDuckMessageLabels {
    public static final String LABEL_PROJECT = "Project";
    public static final String LABEL_PROJECT_VERSION = "Project Version";
    public static final String LABEL_COMPONENT = "Component";
    public static final String LABEL_COMPONENT_VERSION = "Component Version";
    public static final String LABEL_LICENSE = "License";
    public static final String LABEL_OVERRIDER = "Policy overridden by";

    public static final String VALUE_UNKNOWN_LICENSE = "Unknown License";
    public static final String VALUE_UNKNOWN_USAGE = "Unknown Usage";

    private static final String PREFIX_REMEDIATION = "Remediation";
    public static final String LABEL_REMEDIATION_FIX_PREVIOUS = PREFIX_REMEDIATION + " - Fixes Previous Vulnerabilities";
    public static final String LABEL_REMEDIATION_CLEAN = PREFIX_REMEDIATION + " - Without Vulnerabilities";
    public static final String LABEL_REMEDIATION_LATEST = PREFIX_REMEDIATION + " - Latest Version";

    private static final String PREFIX_GUIDANCE = "Upgrade Guidance";
    public static final String LABEL_GUIDANCE_SHORT_TERM = PREFIX_GUIDANCE + " - Short term";
    public static final String LABEL_GUIDANCE_LONG_TERM = PREFIX_GUIDANCE + " - Long term";

    private BlackDuckMessageLabels() {
    }

}
