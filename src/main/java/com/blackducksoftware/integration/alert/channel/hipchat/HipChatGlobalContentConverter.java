package com.blackducksoftware.integration.alert.channel.hipchat;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.ContentConverter;
import com.blackducksoftware.integration.alert.channel.hipchat.model.HipChatGlobalConfigEntity;
import com.blackducksoftware.integration.alert.channel.hipchat.model.HipChatGlobalConfigRestModel;
import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.web.model.ConfigRestModel;

@Component
public class HipChatGlobalContentConverter extends DatabaseContentConverter {
    private final ContentConverter contentConverter;

    @Autowired
    public HipChatGlobalContentConverter(final ContentConverter contentConverter) {
        this.contentConverter = contentConverter;
    }

    @Override
    public Optional<? extends ConfigRestModel> getRestModelFromJson(final String json) {
        return contentConverter.getContent(json, HipChatGlobalConfigRestModel.class);
    }

    @Override
    public DatabaseEntity populateDatabaseEntityFromRestModel(final ConfigRestModel restModel) {
        final HipChatGlobalConfigRestModel hipChatRestModel = (HipChatGlobalConfigRestModel) restModel;
        final HipChatGlobalConfigEntity hipChatEntity = new HipChatGlobalConfigEntity(hipChatRestModel.getApiKey(), hipChatRestModel.getHostServer());
        return hipChatEntity;
    }

    @Override
    public ConfigRestModel populateRestModelFromDatabaseEntity(final DatabaseEntity entity) {
        final HipChatGlobalConfigEntity hipChatEntity = (HipChatGlobalConfigEntity) entity;
        final String id = String.valueOf(hipChatEntity.getId());
        final boolean isApiKeySet = StringUtils.isNotBlank(hipChatEntity.getApiKey());
        final HipChatGlobalConfigRestModel hipChatRestModel = new HipChatGlobalConfigRestModel(id, hipChatEntity.getApiKey(), isApiKeySet, hipChatEntity.getHostServer());
        return hipChatRestModel;
    }

}
