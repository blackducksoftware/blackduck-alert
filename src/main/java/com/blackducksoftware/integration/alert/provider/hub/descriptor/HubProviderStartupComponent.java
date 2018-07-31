package com.blackducksoftware.integration.alert.provider.hub.descriptor;

import java.util.Set;

import com.blackducksoftware.integration.alert.common.descriptor.config.StartupComponent;
import com.blackducksoftware.integration.alert.database.entity.EntityPropertyMapper;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalHubConfigEntity;
import com.blackducksoftware.integration.alert.web.provider.hub.GlobalHubConfig;
import com.blackducksoftware.integration.alert.workflow.startup.AlertStartupProperty;

public class HubProviderStartupComponent extends StartupComponent {
    private final EntityPropertyMapper entityPropertyMapper;

    public HubProviderStartupComponent(final EntityPropertyMapper entityPropertyMapper) {
        super(new GlobalHubConfig());
        this.entityPropertyMapper = entityPropertyMapper;
    }

    @Override
    public Set<AlertStartupProperty> getGlobalEntityPropertyMapping() {
        return entityPropertyMapper.mapEntityToProperties(HubDescriptor.PROVIDER_NAME, GlobalHubConfigEntity.class);
    }

}
