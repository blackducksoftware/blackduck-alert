/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.audit;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

@Component
public final class AuditDescriptorKey extends DescriptorKey {
    private static final String AUDIT_COMPONENT = "component_audit";

    public AuditDescriptorKey() {
        super(AUDIT_COMPONENT, AuditDescriptor.AUDIT_LABEL);
    }

}
