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
package com.blackducksoftware.integration.hub.alert.channel.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.channel.email.controller.distribution.EmailGroupDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.distribution.EmailGroupDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.distribution.EmailGroupDistributionRepository;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailRepository;
import com.blackducksoftware.integration.hub.alert.channel.manager.DistributionChannelManager;
import com.blackducksoftware.integration.hub.alert.digest.model.DigestModel;
import com.blackducksoftware.integration.hub.alert.event.AlertEventContentConverter;
import com.blackducksoftware.integration.hub.alert.event.ChannelEvent;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;

@Component
public class EmailGroupManager extends DistributionChannelManager<GlobalEmailConfigEntity, EmailGroupDistributionConfigEntity, EmailGroupDistributionRestModel> {

    @Autowired
    public EmailGroupManager(final EmailGroupChannel distributionChannel, final GlobalEmailRepository globalRepository, final EmailGroupDistributionRepository localRepository, final ObjectTransformer objectTransformer,
            final AlertEventContentConverter contentConverter) {
        super(distributionChannel, globalRepository, localRepository, objectTransformer, contentConverter);
    }

    @Override
    public boolean isApplicable(final String supportedChannelName) {
        return EmailGroupChannel.COMPONENT_NAME.equals(supportedChannelName);
    }

    @Override
    public ChannelEvent createChannelEvent(final DigestModel content, final Long commonDistributionConfigId) {
        return createChannelEvent(EmailGroupChannel.COMPONENT_NAME, content, commonDistributionConfigId);
    }

    @Override
    public String sendTestMessage(final EmailGroupDistributionRestModel restModel) throws AlertException {
        if (getDistributionChannel().getGlobalConfigEntity() != null) {
            return super.sendTestMessage(restModel);
        }
        return "ERROR: Missing global configuration!";
    }

    @Override
    public Class<EmailGroupDistributionConfigEntity> getDatabaseEntityClass() {
        return EmailGroupDistributionConfigEntity.class;
    }

}
