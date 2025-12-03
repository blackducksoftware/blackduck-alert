/*
 * blackduck-alert
 *
 * Copyright (c) 2025 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.startup.component;

import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.api.event.NotificationReceivedEvent;
import com.blackduck.integration.alert.api.provider.Provider;
import com.blackduck.integration.alert.api.provider.ProviderDescriptor;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(80)
public class NotificationMappingInitializer extends StartupComponent {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final List<Provider> providers;
    private final ConfigurationModelConfigurationAccessor providerConfigurationAccessor;
    private final NotificationAccessor notificationAccessor;
    private final EventManager eventManager;

    @Autowired
    public NotificationMappingInitializer(List<Provider> providers, ConfigurationModelConfigurationAccessor providerConfigurationAccessor, NotificationAccessor notificationAccessor, EventManager eventManager) {
        this.providers = providers;
        this.providerConfigurationAccessor = providerConfigurationAccessor;
        this.notificationAccessor = notificationAccessor;
        this.eventManager = eventManager;
    }

    @Override
    protected void initialize() {
        for (Provider provider : providers) {
            List<ConfigurationModel> providerConfigurations = providerConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(provider.getKey(), ConfigContextEnum.GLOBAL);
            for (ConfigurationModel providerConfiguration : providerConfigurations) {
                boolean providerEnabled = providerConfiguration.getField(ProviderDescriptor.KEY_PROVIDER_CONFIG_ENABLED)
                        .flatMap(ConfigurationFieldModel::getFieldValue)
                        .map(Boolean::parseBoolean)
                        .orElse(false);
                // only start processing for enabled provider configurations.
                if(providerEnabled) {
                    long providerConfigId = providerConfiguration.getConfigurationId();
                    AlertPagedModel<AlertNotificationModel> incompleteMappingNotifications = notificationAccessor.getFirstPageOfNotificationsNotProcessed(providerConfigId, 1);
                    if (!incompleteMappingNotifications.getModels().isEmpty()) {
                        // send event to start mapping notifications for provided
                        String providerName = providerConfiguration.getField(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME)
                                .flatMap(ConfigurationFieldModel::getFieldValue)
                                .orElse(StringUtils.EMPTY);
                        logger.info("Restarting notification mappings for provider: {}({})", providerName, providerConfigId);
                        notificationAccessor.setNotificationsMappingFalseWhenProcessedFalse(providerConfigId);
                        eventManager.sendEvent(new NotificationReceivedEvent(providerConfiguration.getConfigurationId()));
                    }
                }
            }
        }
    }
}
