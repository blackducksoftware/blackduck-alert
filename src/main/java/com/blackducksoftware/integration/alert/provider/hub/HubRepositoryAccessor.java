package com.blackducksoftware.integration.alert.provider.hub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.descriptor.RepositoryAccessor;
import com.blackducksoftware.integration.alert.provider.hub.model.GlobalHubConfigEntity;
import com.blackducksoftware.integration.alert.provider.hub.model.GlobalHubRepository;

@Component
public class HubRepositoryAccessor extends RepositoryAccessor {
    private final GlobalHubRepository repository;

    @Autowired
    public HubRepositoryAccessor(final GlobalHubRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    public DatabaseEntity saveEntity(final DatabaseEntity entity) {
        final GlobalHubConfigEntity hubEntity = (GlobalHubConfigEntity) entity;
        return repository.save(hubEntity);
    }

}
