package com.synopsys.integration.alert.common.descriptor.config;

import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.web.actions.ConfiguredProjectsActions;
import com.synopsys.integration.alert.web.actions.NotificationTypesActions;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;
import com.synopsys.integration.alert.web.model.Config;

public class CommonTypeConverter {
    private final ConfiguredProjectsActions configuredProjectsActions;
    private final NotificationTypesActions notificationTypesActions;
    private final ContentConverter contentConverter;

    @Autowired
    public CommonTypeConverter(final ConfiguredProjectsActions configuredProjectsActions, final NotificationTypesActions notificationTypesActions, final ContentConverter contentConverter) {
        this.configuredProjectsActions = configuredProjectsActions;
        this.notificationTypesActions = notificationTypesActions;
        this.contentConverter = contentConverter;
    }

    public Config populateCommonFieldsFromEntity(final CommonDistributionConfig channelConfig, final CommonDistributionConfigEntity commonEntity) {
        channelConfig.setId(contentConverter.getStringValue(commonEntity.getId()));
        channelConfig.setDistributionType(commonEntity.getDistributionType());
        channelConfig.setFilterByProject(contentConverter.getStringValue(commonEntity.getFilterByProject()));
        channelConfig.setFrequency(commonEntity.getFrequency().name());
        channelConfig.setName(commonEntity.getName());
        channelConfig.setConfiguredProjects(configuredProjectsActions.getConfiguredProjects(commonEntity));
        channelConfig.setNotificationTypes(notificationTypesActions.getNotificationTypes(commonEntity));
        return channelConfig;
    }
}
