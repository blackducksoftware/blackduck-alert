/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.alert.web.actions;

import java.util.Collections;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.channel.slack.SlackChannel;
import com.blackducksoftware.integration.hub.alert.datasource.entity.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.SlackDistributionRestModel;

public class SlackConfigActions extends ConfigActions<SlackDistributionConfigEntity, SlackDistributionRestModel> {
    SlackChannel slackChannel;

    public SlackConfigActions(final SlackChannel slackChannel, final Class<SlackDistributionConfigEntity> databaseEntityClass, final Class<SlackDistributionRestModel> configRestModelClass,
            final JpaRepository<SlackDistributionConfigEntity, Long> repository, final ObjectTransformer objectTransformer) {
        super(databaseEntityClass, configRestModelClass, repository, objectTransformer);
        this.slackChannel = slackChannel;
    }

    @Override
    public List<String> sensitiveFields() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public String validateConfig(final SlackDistributionRestModel restModel) throws AlertFieldException {
        return "Valid";
    }

    @Override
    public String channelTestConfig(final SlackDistributionRestModel restModel) throws IntegrationException {
        final SlackDistributionConfigEntity config = objectTransformer.configRestModelToDatabaseEntity(restModel, SlackDistributionConfigEntity.class);
        return slackChannel.testMessage(config);
    }

}
