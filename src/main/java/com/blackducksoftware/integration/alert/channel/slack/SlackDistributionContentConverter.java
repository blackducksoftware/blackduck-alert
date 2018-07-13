package com.blackducksoftware.integration.alert.channel.slack;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.ContentConverter;
import com.blackducksoftware.integration.alert.channel.slack.model.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.alert.channel.slack.model.SlackDistributionRestModel;
import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.descriptor.DatabaseContentConverter;
import com.blackducksoftware.integration.alert.web.model.ConfigRestModel;

@Component
public class SlackDistributionContentConverter extends DatabaseContentConverter {
    private final ContentConverter contentConverter;

    @Autowired
    public SlackDistributionContentConverter(final ContentConverter contentConverter) {
        this.contentConverter = contentConverter;
    }

    @Override
    public ConfigRestModel getRestModelFromJson(final String json) {
        final Optional<SlackDistributionRestModel> restModel = contentConverter.getContent(json, SlackDistributionRestModel.class);
        if (restModel.isPresent()) {
            return restModel.get();
        }

        return null;
    }

    @Override
    public DatabaseEntity populateDatabaseEntityFromRestModel(final ConfigRestModel restModel) {
        final SlackDistributionRestModel slackRestModel = (SlackDistributionRestModel) restModel;
        return new SlackDistributionConfigEntity(slackRestModel.getWebhook(), slackRestModel.getChannelUsername(), slackRestModel.getChannelName());
    }

    @Override
    public ConfigRestModel populateRestModelFromDatabaseEntity(final DatabaseEntity entity) {
        final SlackDistributionConfigEntity slackEntity = (SlackDistributionConfigEntity) entity;
        final SlackDistributionRestModel slackRestModel = new SlackDistributionRestModel();
        slackRestModel.setWebhook(slackEntity.getWebhook());
        slackRestModel.setChannelUsername(slackEntity.getChannelUsername());
        slackRestModel.setChannelName(slackEntity.getChannelName());
        return slackRestModel;
    }

}
