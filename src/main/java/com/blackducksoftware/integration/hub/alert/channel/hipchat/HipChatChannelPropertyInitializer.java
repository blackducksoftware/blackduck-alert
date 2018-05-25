package com.blackducksoftware.integration.hub.alert.channel.hipchat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.channel.AbstractChannelPropertyInitializer;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.global.GlobalHipChatConfigRestModel;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global.GlobalHipChatRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

@Component
public class HipChatChannelPropertyInitializer extends AbstractChannelPropertyInitializer<GlobalHipChatConfigEntity> {
    public static final String PROPERTY_PREFIX_CHANNEL_HIPCHAT = "CHANNEL_HIPCHAT";
    private final static Logger logger = LoggerFactory.getLogger(HipChatChannelPropertyInitializer.class);
    private final GlobalHipChatRepositoryWrapper globalHipChatRepository;

    @Autowired
    public HipChatChannelPropertyInitializer(final GlobalHipChatRepositoryWrapper globalHipChatRepository) {
        this.globalHipChatRepository = globalHipChatRepository;
    }

    @Override
    public String getPropertyNamePrefix() {
        return PROPERTY_PREFIX_CHANNEL_HIPCHAT;
    }

    @Override
    public Class<GlobalHipChatConfigEntity> getEntityClass() {
        return GlobalHipChatConfigEntity.class;
    }

    @Override
    public ConfigRestModel getRestModelInstance() {
        return new GlobalHipChatConfigRestModel();
    }

    @Override
    public void save(final DatabaseEntity entity) {
        logger.info("Saving HipChat channel global properties {}", entity);
        // ps - dislike that I have to do this at all but this is the only place where the check is made.
        if (entity instanceof GlobalHipChatConfigEntity) {
            final GlobalHipChatConfigEntity entityToSave = (GlobalHipChatConfigEntity) entity;
            this.globalHipChatRepository.save(entityToSave);
        }
    }

    @Override
    public boolean canSetDefaultProperties() {
        return globalHipChatRepository.findAll().isEmpty();
    }

}
