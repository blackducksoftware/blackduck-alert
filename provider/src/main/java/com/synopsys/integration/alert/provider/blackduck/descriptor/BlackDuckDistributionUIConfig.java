/**
 * provider
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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.EndpointSelectField;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.table.EndpointTableSelectField;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.table.TableSelectColumn;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;

@Component
public class BlackDuckDistributionUIConfig extends ProviderDistributionUIConfig {
    private static final String LABEL_BLACKDUCK_POLICY_NOTIFICATION_TYPE_FILTER = "Policy Notification Type Filter";
    private static final String LABEL_BALCKDUCK_VULNERABILITY_NOTIFICATION_TYPE_FILTER = "Vulnerability Notification Type Filter";

    private static final String DESCRIPTION_BLACKDUCK_POLICY_NOTIFICATION_TYPE_FILTER = "List of Policies you can choose from to further filter which notifications you want sent via this job (You must have a policy notification selected for this filter to apply).";
    private static final String DESCRIPTION_BLACKDUCK_VULNERABILITY_NOTIFICATION_TYPE_FILTER = "List of Vulnerability severities you can choose from to further filter which notifications you want sent via this job (You must have a vulnerability notification selected for this filter to apply).";

    private static final String PANEL_NOTIFICATION_FILTERING = "Black Duck Notification Filtering";

    @Autowired
    public BlackDuckDistributionUIConfig(BlackDuckContent blackDuckContent, ConfigurationAccessor configurationAccessor) {
        super(BlackDuckDescriptor.BLACKDUCK_LABEL, BlackDuckDescriptor.BLACKDUCK_URL, blackDuckContent, configurationAccessor);
    }

    @Override
    public List<ConfigField> createProviderDistributionFields() {
        ConfigField policyNotificationTypeFilter = new EndpointTableSelectField(BlackDuckDescriptor.KEY_BLACKDUCK_POLICY_NOTIFICATION_TYPE_FILTER, LABEL_BLACKDUCK_POLICY_NOTIFICATION_TYPE_FILTER,
            DESCRIPTION_BLACKDUCK_POLICY_NOTIFICATION_TYPE_FILTER)
                                                       .applyColumn(TableSelectColumn.visible("name", "Name", true, true))
                                                       .applyPaged(true)
                                                       .applyRequiredRelatedField(ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES)
                                                       .applyRequiredRelatedField(ChannelDistributionUIConfig.KEY_PROVIDER_NAME)
                                                       .applyRequiredRelatedField(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID)
                                                       .applyPanel(PANEL_NOTIFICATION_FILTERING);

        ConfigField vulnerabilityNotificationTypeFilter = new EndpointSelectField(BlackDuckDescriptor.KEY_BLACKDUCK_VULNERABILITY_NOTIFICATION_TYPE_FILTER, LABEL_BALCKDUCK_VULNERABILITY_NOTIFICATION_TYPE_FILTER,
            DESCRIPTION_BLACKDUCK_VULNERABILITY_NOTIFICATION_TYPE_FILTER)
                                                              .applyMultiSelect(true)
                                                              .applyClearable(true)
                                                              .applyPanel(PANEL_NOTIFICATION_FILTERING)
                                                              .applyRequiredRelatedField(ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES);

        return List.of(policyNotificationTypeFilter, vulnerabilityNotificationTypeFilter);
    }

}
