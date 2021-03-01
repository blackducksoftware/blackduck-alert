/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.descriptor;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.provider.ProviderContent;
import com.synopsys.integration.alert.common.provider.ProviderNotificationType;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

@Component
public class BlackDuckContent extends ProviderContent {
    private static final Set<ProviderNotificationType> SUPPORTED_CONTENT_TYPES = fromNotificationTypes(
        NotificationType.LICENSE_LIMIT,
        NotificationType.POLICY_OVERRIDE,
        NotificationType.RULE_VIOLATION,
        NotificationType.RULE_VIOLATION_CLEARED,
        NotificationType.VULNERABILITY,
        NotificationType.BOM_EDIT,
        NotificationType.PROJECT,
        NotificationType.PROJECT_VERSION
    );

    private static final EnumSet<ProcessingType> SUPPORTED_PROCESSING_TYPES = EnumSet.of(ProcessingType.DEFAULT, ProcessingType.DIGEST, ProcessingType.SUMMARY);

    @Autowired
    public BlackDuckContent() {
        super(SUPPORTED_CONTENT_TYPES, SUPPORTED_PROCESSING_TYPES);
    }

    private static Set<ProviderNotificationType> fromNotificationTypes(NotificationType... notificationTypes) {
        return Arrays
                   .stream(notificationTypes)
                   .map(NotificationType::name)
                   .map(ProviderNotificationType::new)
                   .collect(Collectors.toSet());
    }

}
