/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.audit;

import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;

@Component
public final class AuditDescriptorKey extends DescriptorKey {
    private static final String AUDIT_COMPONENT = "component_audit";

    public AuditDescriptorKey() {
        super(AUDIT_COMPONENT, AuditDescriptor.AUDIT_LABEL);
    }

}
