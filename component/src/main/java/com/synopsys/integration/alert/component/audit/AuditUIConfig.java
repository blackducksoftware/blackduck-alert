/*
 * component
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.audit;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.ui.CustomUIConfig;

@Component
public class AuditUIConfig extends CustomUIConfig {

    public AuditUIConfig() {
        super(AuditDescriptor.AUDIT_LABEL, AuditDescriptor.AUDIT_DESCRIPTION, AuditDescriptor.AUDIT_URL, AuditDescriptor.AUDIT_COMPONENT_NAMESPACE);
    }

}
