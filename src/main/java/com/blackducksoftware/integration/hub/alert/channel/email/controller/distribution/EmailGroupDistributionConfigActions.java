/**
 * hub-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.hub.alert.channel.email.controller.distribution;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.channel.email.EmailGroupChannel;
import com.blackducksoftware.integration.hub.alert.channel.email.EmailGroupManager;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.distribution.EmailGroupDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.distribution.EmailGroupDistributionRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.ConfiguredProjectsActions;
import com.blackducksoftware.integration.hub.alert.web.actions.NotificationTypesActions;
import com.blackducksoftware.integration.hub.alert.web.actions.distribution.DistributionConfigActions;

@Component
public class EmailGroupDistributionConfigActions extends DistributionConfigActions<EmailGroupDistributionConfigEntity, EmailGroupDistributionRestModel, EmailGroupDistributionRepository> {
    private final EmailGroupManager emailManager;

    @Autowired
    public EmailGroupDistributionConfigActions(final CommonDistributionRepository commonDistributionRepository, final EmailGroupDistributionRepository repository,
            final ConfiguredProjectsActions<EmailGroupDistributionRestModel> configuredProjectsActions, final NotificationTypesActions<EmailGroupDistributionRestModel> notificationTypesActions, final ObjectTransformer objectTransformer,
            final EmailGroupManager emailManager) {
        super(EmailGroupDistributionConfigEntity.class, EmailGroupDistributionRestModel.class, commonDistributionRepository, repository, configuredProjectsActions, notificationTypesActions, objectTransformer);
        this.emailManager = emailManager;
    }

    @Override
    public EmailGroupDistributionRestModel constructRestModel(final CommonDistributionConfigEntity commonEntity, final EmailGroupDistributionConfigEntity distributionEntity) throws AlertException {
        final EmailGroupDistributionRestModel restModel = getObjectTransformer().databaseEntityToConfigRestModel(commonEntity, EmailGroupDistributionRestModel.class);
        restModel.setId(getObjectTransformer().objectToString(commonEntity.getId()));
        restModel.setGroupName(distributionEntity.getGroupName());
        restModel.setEmailTemplateLogoImage(distributionEntity.getEmailTemplateLogoImage());
        restModel.setEmailSubjectLine(distributionEntity.getEmailSubjectLine());
        return restModel;
    }

    @Override
    public String channelTestConfig(final EmailGroupDistributionRestModel restModel) throws IntegrationException {
        return emailManager.sendTestMessage(restModel);
    }

    @Override
    public String getDistributionName() {
        return EmailGroupChannel.COMPONENT_NAME;
    }

    @Override
    public void validateDistributionConfig(final EmailGroupDistributionRestModel restModel, final Map<String, String> fieldErrors) throws AlertFieldException {
        if (StringUtils.isBlank(restModel.getGroupName())) {
            fieldErrors.put("groupName", "A group must be specified.");
        }
    }

}
