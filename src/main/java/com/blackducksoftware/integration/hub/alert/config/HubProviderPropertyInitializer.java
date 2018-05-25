package com.blackducksoftware.integration.hub.alert.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.hub.alert.channel.AbstractChannelPropertyInitializer;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHubConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHubRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.hub.controller.global.GlobalHubConfigRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

public class HubProviderPropertyInitializer extends AbstractChannelPropertyInitializer<GlobalHubConfigEntity> {
    public static final String PROPERTY_PREFIX_PROVIDER_HUB = "PROVIDER_HUB";
    private final static Logger logger = LoggerFactory.getLogger(HubProviderPropertyInitializer.class);
    private final GlobalHubRepositoryWrapper globalHubRepository;

    @Autowired
    public HubProviderPropertyInitializer(final GlobalHubRepositoryWrapper globalHubRepository) {
        this.globalHubRepository = globalHubRepository;
    }

    @Override
    public String getPropertyNamePrefix() {
        return PROPERTY_PREFIX_PROVIDER_HUB;
    }

    @Override
    public Class<GlobalHubConfigEntity> getEntityClass() {
        return GlobalHubConfigEntity.class;
    }

    @Override
    public ConfigRestModel getRestModelInstance() {
        return new GlobalHubConfigRestModel();
    }

    @Override
    public void save(final DatabaseEntity entity) {
        logger.info("Saving Hub provider global properties {}", entity);
        // ps - dislike that I have to do this at all but this is the only place where the check is made.
        if (entity instanceof GlobalHubConfigEntity) {
            final GlobalHubConfigEntity entityToSave = (GlobalHubConfigEntity) entity;
            this.globalHubRepository.save(entityToSave);
        }
    }

    @Override
    public boolean canSetDefaultProperties() {
        return globalHubRepository.findAll().isEmpty();
    }
}
