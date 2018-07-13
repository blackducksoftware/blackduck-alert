package com.blackducksoftware.integration.alert.provider.hub;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.ContentConverter;
import com.blackducksoftware.integration.alert.channel.hipchat.model.HipChatGlobalConfigRestModel;
import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.descriptor.DatabaseContentConverter;
import com.blackducksoftware.integration.alert.provider.hub.model.GlobalHubConfigEntity;
import com.blackducksoftware.integration.alert.provider.hub.model.GlobalHubConfigRestModel;
import com.blackducksoftware.integration.alert.web.model.ConfigRestModel;

@Component
public class HubContentConverter extends DatabaseContentConverter {
    private final ContentConverter contentConverter;

    @Autowired
    public HubContentConverter(final ContentConverter contentConverter) {
        this.contentConverter = contentConverter;
    }

    @Override
    public ConfigRestModel getRestModelFromJson(final String json) {
        final Optional<HipChatGlobalConfigRestModel> restModel = contentConverter.getContent(json, HipChatGlobalConfigRestModel.class);
        if (restModel.isPresent()) {
            return restModel.get();
        }
        return null;
    }

    @Override
    public DatabaseEntity populateDatabaseEntityFromRestModel(final ConfigRestModel restModel) {
        final GlobalHubConfigRestModel hubRestModel = (GlobalHubConfigRestModel) restModel;
        final int hubTimeout = Integer.parseInt(hubRestModel.getHubTimeout());
        return new GlobalHubConfigEntity(hubTimeout, hubRestModel.getHubApiKey());
    }

    // TODO This will want to populate more data when ProviderDescriptors are created
    @Override
    public ConfigRestModel populateRestModelFromDatabaseEntity(final DatabaseEntity entity) {
        final GlobalHubConfigEntity hubEntity = (GlobalHubConfigEntity) entity;
        final GlobalHubConfigRestModel hubRestModel = new GlobalHubConfigRestModel();
        final String hubTimeout = String.valueOf(hubEntity.getHubTimeout());
        hubRestModel.setHubTimeout(hubTimeout);
        hubRestModel.setHubApiKeyIsSet(StringUtils.isNotBlank(hubEntity.getHubApiKey()));
        hubRestModel.setHubApiKey(hubEntity.getHubApiKey());
        return hubRestModel;
    }

}
