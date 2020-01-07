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
package com.synopsys.integration.alert.provider.blackduck.descriptor;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.provider.ProviderContent;
import com.synopsys.integration.alert.common.provider.ProviderNotificationType;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;

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

    private static final EnumSet<FormatType> SUPPORTED_CONTENT_FORMATS = EnumSet.of(FormatType.DEFAULT, FormatType.DIGEST, FormatType.SUMMARY);

    private static Set<ProviderNotificationType> fromNotificationTypes(NotificationType... notificationTypes) {
        return Arrays
                   .stream(notificationTypes)
                   .map(NotificationType::name)
                   .map(ProviderNotificationType::new)
                   .collect(Collectors.toSet());
    }

    @Autowired
    public BlackDuckContent() {
        super(SUPPORTED_CONTENT_TYPES, SUPPORTED_CONTENT_FORMATS);
    }

}
