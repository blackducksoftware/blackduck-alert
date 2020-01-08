/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.component.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.ComponentDescriptor;

@Component
public class AuditDescriptor extends ComponentDescriptor {
    public static final String AUDIT_COMPONENT = "component_audit";

    public static final String AUDIT_LABEL = "Audit";
    public static final String AUDIT_URL = "audit";
    public static final String AUDIT_DESCRIPTION = "Audit tracks all distribution events that have been produced by Alert and displays whether the event was successful or not. If an event fails, this page offers the ability to resend that event and see why it failed.";
    public static final String AUDIT_COMPONENT_NAMESPACE = "audit.AuditPage";

    @Autowired
    public AuditDescriptor(AuditDescriptorKey auditDescriptorKey, AuditUIConfig auditUIConfig) {
        super(auditDescriptorKey, auditUIConfig);
    }

}
