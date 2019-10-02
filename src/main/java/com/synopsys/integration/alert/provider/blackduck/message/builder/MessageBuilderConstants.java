/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.provider.blackduck.message.builder;

public class MessageBuilderConstants {
    public static final String LABEL_COMPONENT_VERSION_STATUS = "Component Version Status";
    public static final String LABEL_COMPONENT_NAME = "Component";
    public static final String LABEL_COMPONENT_VERSION_NAME = "Component Version";
    public static final String LABEL_COMPONENT_LICENSE = "Component License";
    public static final String LABEL_COMPONENT_USAGE = "Component Usage";
    public static final String LABEL_POLICY_INFO_LIST = "Policy Infos";
    public static final String LABEL_POLICY_NAME = "Policy Violated";
    public static final String LABEL_POLICY_SEVERITY_NAME = "Severity";
    public static final String LABEL_PROJECT_NAME = "Project";
    public static final String LABEL_PROJECT_VERSION_NAME = "Project Version";
    public static final String LABEL_POLICY_OVERRIDE_FIRST_NAME = "First Name";
    public static final String LABEL_POLICY_OVERRIDE_LAST_NAME = "Last Name";
    public static final String LABEL_POLICY_OVERRIDE_BY = "Policy Overridden by";
    public static final String LABEL_LICENSE_LIMIT_MESSAGE = "License Limit Message";
    public static final String LABEL_LICENSE_LIMIT_USED_CODE_SIZE = "Used Code Size";
    public static final String LABEL_LICENSE_LIMIT_HARD = "Hard Limit";
    public static final String LABEL_LICENSE_LIMIT_SOFT = "Soft Limit";

    public static final String LABEL_VULNERABILITY_NEW = "New Vulnerabilities";
    public static final String LABEL_VULNERABILITY_UPDATED = "Updated Vulnerabilities";
    public static final String LABEL_VULNERABILITY_DELETED = "Deleted Vulnerabilities";
    public static final String LABEL_VULNERABILITIES = "Vulnerabilities";
    public static final String LABEL_VULNERABILITY_SEVERITY = "Severity";

    public static final String LABEL_BOM_COMPONENT = "Bom Component";
    public static final String LABEL_OPERATION_TYPE = "Operation Type";

    public static final String LABEL_REMEDIATION_FIX_PREVIOUS = "Remediation - Fixes Previous Vulnerabilities";
    public static final String LABEL_REMEDIATION_CLEAN = "Remediation - Without Vulnerabilities";
    public static final String LABEL_REMEDIATION_LATEST = "Remediation - Latest Version";
}
