/**
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.provider.blackduck;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.provider.Provider;
import com.synopsys.integration.alert.common.provider.lifecycle.ProviderTask;
import com.synopsys.integration.alert.common.provider.notification.ProviderDistributionFilter;
import com.synopsys.integration.alert.common.provider.notification.ProviderNotificationClassMap;
import com.synopsys.integration.alert.common.provider.state.StatefulProvider;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.collector.BlackDuckMessageContentCollector;
import com.synopsys.integration.alert.provider.blackduck.collector.MessageContentCollectorFactory;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckContent;
import com.synopsys.integration.alert.provider.blackduck.factory.BlackDuckPropertiesFactory;
import com.synopsys.integration.alert.provider.blackduck.factory.BlackDuckTaskFactory;
import com.synopsys.integration.alert.provider.blackduck.factory.DistributionFilterFactory;
import com.synopsys.integration.alert.provider.blackduck.validator.BlackDuckValidator;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.BomEditNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.LicenseLimitNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.PolicyOverrideNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.ProjectNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.ProjectVersionNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.RuleViolationClearedNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.RuleViolationNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.VersionBomCodeLocationBomComputedNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.VulnerabilityNotificationView;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;

@Component
public class BlackDuckProvider extends Provider {
    private final Logger logger = LoggerFactory.getLogger(BlackDuckProvider.class);
    private final DistributionFilterFactory distributionFilterFactory;
    private final MessageContentCollectorFactory messageContentCollectorFactory;
    private final BlackDuckPropertiesFactory propertiesFactory;
    private final BlackDuckValidator validator;
    private final BlackDuckTaskFactory taskFactory;

    @Autowired
    public BlackDuckProvider(BlackDuckProviderKey blackDuckProviderKey, BlackDuckContent blackDuckContent, DistributionFilterFactory distributionFilterFactory, MessageContentCollectorFactory messageContentCollectorFactory,
        BlackDuckPropertiesFactory propertiesFactory, BlackDuckValidator validator, BlackDuckTaskFactory taskFactory) {
        super(blackDuckProviderKey, blackDuckContent);
        this.distributionFilterFactory = distributionFilterFactory;
        this.messageContentCollectorFactory = messageContentCollectorFactory;
        this.propertiesFactory = propertiesFactory;
        this.validator = validator;
        this.taskFactory = taskFactory;
    }

    @Override
    public boolean validate(ConfigurationModel configurationModel) {
        BlackDuckProperties blackDuckProperties = propertiesFactory.createProperties(configurationModel);
        return validator.validate(blackDuckProperties);
    }

    @Override
    public StatefulProvider createStatefulProvider(ConfigurationModel configurationModel) throws AlertException {
        BlackDuckProperties blackDuckProperties = propertiesFactory.createProperties(configurationModel);
        List<ProviderTask> tasks = taskFactory.createTasks(blackDuckProperties);
        BlackDuckServicesFactory blackDuckServicesFactory = createBlackDuckServicesFactory(blackDuckProperties);
        ProviderDistributionFilter distributionFilter = distributionFilterFactory.createFilter(blackDuckServicesFactory, getNotificationClassMap());
        BlackDuckMessageContentCollector messageContentCollector = messageContentCollectorFactory.createCollector(blackDuckServicesFactory);

        return StatefulProvider.create(getKey(), configurationModel, tasks, blackDuckProperties, distributionFilter, messageContentCollector);
    }

    private BlackDuckServicesFactory createBlackDuckServicesFactory(BlackDuckProperties blackDuckProperties) throws AlertException {
        IntLogger intLogger = new Slf4jIntLogger(logger);
        BlackDuckHttpClient blackDuckHttpClient = blackDuckProperties.createBlackDuckCacheClient(intLogger);
        return blackDuckProperties.createBlackDuckServicesFactory(blackDuckHttpClient, intLogger);
    }

    @Override
    public ProviderNotificationClassMap getNotificationClassMap() {
        Map<String, Class<?>> notificationTypeToContentClass = new HashMap<>();
        notificationTypeToContentClass.put(NotificationType.BOM_EDIT.name(), BomEditNotificationView.class);
        notificationTypeToContentClass.put(NotificationType.LICENSE_LIMIT.name(), LicenseLimitNotificationView.class);
        notificationTypeToContentClass.put(NotificationType.POLICY_OVERRIDE.name(), PolicyOverrideNotificationView.class);
        notificationTypeToContentClass.put(NotificationType.PROJECT.name(), ProjectNotificationView.class);
        notificationTypeToContentClass.put(NotificationType.PROJECT_VERSION.name(), ProjectVersionNotificationView.class);
        notificationTypeToContentClass.put(NotificationType.RULE_VIOLATION.name(), RuleViolationNotificationView.class);
        notificationTypeToContentClass.put(NotificationType.RULE_VIOLATION_CLEARED.name(), RuleViolationClearedNotificationView.class);
        notificationTypeToContentClass.put(NotificationType.VERSION_BOM_CODE_LOCATION_BOM_COMPUTED.name(), VersionBomCodeLocationBomComputedNotificationView.class);
        notificationTypeToContentClass.put(NotificationType.VULNERABILITY.name(), VulnerabilityNotificationView.class);
        return new ProviderNotificationClassMap(notificationTypeToContentClass);
    }

}
