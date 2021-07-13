/*
 * component
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.audit;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.ComponentDescriptor;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationValidator;

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

    @Override
    public Optional<GlobalConfigurationValidator> getGlobalValidator() {
        return Optional.empty();
    }
}
