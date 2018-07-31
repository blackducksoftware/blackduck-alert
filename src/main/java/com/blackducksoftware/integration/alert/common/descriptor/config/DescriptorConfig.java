package com.blackducksoftware.integration.alert.common.descriptor.config;

import java.util.Map;

import com.blackducksoftware.integration.alert.database.RepositoryAccessor;
import com.blackducksoftware.integration.alert.database.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.web.model.Config;
import com.blackducksoftware.integration.exception.IntegrationException;

public abstract class DescriptorConfig {
    private final DatabaseContentConverter databaseContentConverter;
    private final RepositoryAccessor repositoryAccessor;
    private StartupComponent startupComponent;

    public DescriptorConfig(final DatabaseContentConverter databaseContentConverter, final RepositoryAccessor repositoryAccessor) {
        this.databaseContentConverter = databaseContentConverter;
        this.repositoryAccessor = repositoryAccessor;
        this.startupComponent = null;
    }

    public DatabaseContentConverter getDatabaseContentConverter() {
        return databaseContentConverter;
    }

    public RepositoryAccessor getRepositoryAccessor() {
        return repositoryAccessor;
    }

    public void setStartupComponent(final StartupComponent startupComponent) {
        this.startupComponent = startupComponent;
    }

    public StartupComponent getStartupComponent() {
        return startupComponent;
    }

    public boolean hasStartupProperties() {
        return getStartupComponent() != null;
    }

    public abstract UIComponent getUiComponent();

    public abstract void validateConfig(Config restModel, Map<String, String> fieldErrors);

    public abstract void testConfig(DatabaseEntity entity) throws IntegrationException;

}
