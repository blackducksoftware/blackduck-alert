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
package com.synopsys.integration.alert.provider.polaris.descriptor;

import java.util.EnumSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.provider.ProviderContent;
import com.synopsys.integration.alert.common.provider.ProviderNotificationType;
import com.synopsys.integration.alert.provider.polaris.model.AlertPolarisNotificationTypeEnum;

//@Component
public class PolarisContent extends ProviderContent {
    public static final ProviderNotificationType ISSUE_COUNT_INCREASED = new ProviderNotificationType(AlertPolarisNotificationTypeEnum.ISSUE_COUNT_INCREASED.name());
    public static final ProviderNotificationType ISSUE_COUNT_DECREASED = new ProviderNotificationType(AlertPolarisNotificationTypeEnum.ISSUE_COUNT_DECREASED.name());

    @Autowired
    public PolarisContent() {
        super(Set.of(ISSUE_COUNT_INCREASED, ISSUE_COUNT_DECREASED), EnumSet.of(FormatType.DEFAULT, FormatType.SUMMARY));
    }

}
