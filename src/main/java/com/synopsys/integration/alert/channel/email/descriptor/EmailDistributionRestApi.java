/**
 * blackduck-alert
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
package com.synopsys.integration.alert.channel.email.descriptor;

import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.EmailChannelEvent;
import com.synopsys.integration.alert.channel.email.EmailGroupChannel;
import com.synopsys.integration.alert.channel.event.ChannelEventFactory;
import com.synopsys.integration.alert.common.descriptor.config.RestApi;
import com.synopsys.integration.alert.database.channel.email.EmailDistributionRepositoryAccessor;
import com.synopsys.integration.alert.web.channel.model.EmailDistributionConfig;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class EmailDistributionRestApi extends RestApi {
    private final EmailGroupChannel emailGroupChannel;
    private final ChannelEventFactory channelEventFactory;

    @Autowired
    public EmailDistributionRestApi(final EmailDistributionTypeConverter databaseContentConverter, final EmailDistributionRepositoryAccessor repositoryAccessor, final EmailGroupChannel emailGroupChannel,
        final ChannelEventFactory channelEventFactory) {
        super(databaseContentConverter, repositoryAccessor);
        this.emailGroupChannel = emailGroupChannel;
        this.channelEventFactory = channelEventFactory;
    }

    @Override
    public void validateConfig(final Config restModel, final Map<String, String> fieldErrors) {
        final EmailDistributionConfig emailRestModel = (EmailDistributionConfig) restModel;
        if (StringUtils.isBlank(emailRestModel.getGroupName())) {
            fieldErrors.put("groupName", "A group must be specified.");
        }
        //TODO expose this error in the UI
        if (BooleanUtils.toBoolean(emailRestModel.getFilterByProject()) && null != emailRestModel.getConfiguredProjects() && emailRestModel.getConfiguredProjects().isEmpty()) {
            fieldErrors.put("configuredProjects", "You must select at least one project.");
        }
    }

    @Override
    public void testConfig(final Config restModel) throws IntegrationException {
        final EmailChannelEvent event = channelEventFactory.createEmailChannelTestEvent(restModel);
        emailGroupChannel.sendAuditedMessage(event);
    }
}
