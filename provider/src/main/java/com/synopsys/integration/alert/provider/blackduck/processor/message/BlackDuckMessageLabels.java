/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

    private static final String PREFIX_GUIDANCE = "Guidance";
    public static final String LABEL_GUIDANCE_SHORT_TERM = PREFIX_GUIDANCE + " - Short term";
    public static final String LABEL_GUIDANCE_LONG_TERM = PREFIX_GUIDANCE + " - Long term";

    private BlackDuckMessageLabels() {
    }

}
