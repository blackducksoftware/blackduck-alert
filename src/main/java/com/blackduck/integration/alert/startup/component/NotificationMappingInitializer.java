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
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

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
                if (providerEnabled) {
                    long providerConfigId = providerConfiguration.getConfigurationId();
                    String providerName = providerConfiguration.getField(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME)
                        .flatMap(ConfigurationFieldModel::getFieldValue)
                        .orElse(StringUtils.EMPTY);
                    if (notificationAccessor.hasMoreNotificationsToProcess(providerConfigId)) {
                        notificationAccessor.setNotificationsMappingFalseWhenProcessedFalse(providerConfigId);
                        reprocessNotifications(providerName, providerConfigId);
                    }
                }
            }
        }
    }

    private void reprocessNotifications(final String providerName, final long providerConfigId) {
        PageRequest pageRequest = PageRequest.of(0, 100);
        AlertPagedModel<UUID> batchIdsToReprocess = notificationAccessor.findUniqueBatchesForProviderWithNotificationsNotProcessed(pageRequest, providerConfigId);
        while (pageRequest.getPageNumber() < batchIdsToReprocess.getTotalPages() && !batchIdsToReprocess.getModels().isEmpty()) {
            // send event to start mapping notifications for provided
            for (UUID batchId : batchIdsToReprocess.getModels()) {
                logger.info("Restarting notification mappings for provider: {}({}) batch: {}", providerName, providerConfigId, batchId);
                eventManager.sendEvent(new NotificationReceivedEvent(providerConfigId, batchId));
            }
            pageRequest = pageRequest.next();
            if (pageRequest.getPageNumber() < batchIdsToReprocess.getTotalPages()) {
                batchIdsToReprocess = notificationAccessor.findUniqueBatchesForProviderWithNotificationsNotProcessed(pageRequest, providerConfigId);
            }
        }
    }
}
