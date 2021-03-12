/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.provider.Provider;
import com.synopsys.integration.alert.common.provider.lifecycle.ProviderTask;
import com.synopsys.integration.alert.common.provider.notification.ProviderNotificationClassMap;
import com.synopsys.integration.alert.common.provider.state.StatefulProvider;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckContent;
import com.synopsys.integration.alert.provider.blackduck.factory.BlackDuckPropertiesFactory;
import com.synopsys.integration.alert.provider.blackduck.factory.BlackDuckTaskFactory;
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

@Component
public class BlackDuckProvider extends Provider {
    private final BlackDuckPropertiesFactory propertiesFactory;
    private final BlackDuckValidator validator;
    private final BlackDuckTaskFactory taskFactory;

    @Autowired
    public BlackDuckProvider(BlackDuckProviderKey blackDuckProviderKey, BlackDuckContent blackDuckContent,
        BlackDuckPropertiesFactory propertiesFactory, BlackDuckValidator validator, BlackDuckTaskFactory taskFactory) {
        super(blackDuckProviderKey, blackDuckContent);
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

        return StatefulProvider.create(getKey(), configurationModel, tasks, blackDuckProperties);
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
