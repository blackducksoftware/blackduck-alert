package com.blackducksoftware.integration.alert.provider.hub.descriptor;

import java.util.Map;

import com.blackducksoftware.integration.alert.common.descriptor.config.DescriptorConfig;
import com.blackducksoftware.integration.alert.common.descriptor.config.UIComponent;
import com.blackducksoftware.integration.alert.database.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.web.model.Config;
import com.blackducksoftware.integration.exception.IntegrationException;

public class HubProviderDescriptorConfig extends DescriptorConfig {

    public HubProviderDescriptorConfig(final HubContentConverter databaseContentConverter, final HubRepositoryAccessor repositoryAccessor, final HubProviderStartupComponent startupComponent) {
        super(databaseContentConverter, repositoryAccessor);
        setStartupComponent(startupComponent);
    }

    @Override
    public UIComponent getUiComponent() {
        return new UIComponent("Hub", "laptop", "HubConfiguration");
    }

    @Override
    public void validateConfig(final Config restModel, final Map<String, String> fieldErrors) {

    }

    @Override
    public void testConfig(final DatabaseEntity entity) throws IntegrationException {

    }

}
