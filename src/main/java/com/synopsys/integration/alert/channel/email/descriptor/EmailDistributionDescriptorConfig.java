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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.EmailGroupChannel;
import com.synopsys.integration.alert.channel.event.ChannelEvent;
import com.synopsys.integration.alert.channel.event.ChannelEventFactory;
import com.synopsys.integration.alert.common.descriptor.config.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.DescriptorConfig;
import com.synopsys.integration.alert.common.descriptor.config.UIComponent;
import com.synopsys.integration.alert.common.enumeration.FieldGroup;
import com.synopsys.integration.alert.common.enumeration.FieldType;
import com.synopsys.integration.alert.database.channel.email.EmailDistributionRepositoryAccessor;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionConfigEntity;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.web.channel.model.EmailDistributionConfig;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.alert.web.provider.blackduck.BlackDuckDataActions;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class EmailDistributionDescriptorConfig extends DescriptorConfig {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final EmailGroupChannel emailGroupChannel;
    private final ChannelEventFactory channelEventFactory;
    private final BlackDuckDataActions blackDuckDataActions;

    @Autowired
    public EmailDistributionDescriptorConfig(final EmailDistributionTypeConverter databaseContentConverter, final EmailDistributionRepositoryAccessor repositoryAccessor, final EmailGroupChannel emailGroupChannel,
            final ChannelEventFactory channelEventFactory, final BlackDuckDataActions blackDuckDataActions) {
        super(databaseContentConverter, repositoryAccessor);
        this.emailGroupChannel = emailGroupChannel;
        this.channelEventFactory = channelEventFactory;
        this.blackDuckDataActions = blackDuckDataActions;
    }

    @Override
    public void validateConfig(final Config restModel, final Map<String, String> fieldErrors) {
        final EmailDistributionConfig emailRestModel = (EmailDistributionConfig) restModel;

        if (StringUtils.isBlank(emailRestModel.getGroupName())) {
            fieldErrors.put("groupName", "A group must be specified.");
        }
    }

    @Override
    public void testConfig(final DatabaseEntity entity) throws IntegrationException {
        final EmailGroupDistributionConfigEntity emailEntity = (EmailGroupDistributionConfigEntity) entity;
        final ChannelEvent event = channelEventFactory.createChannelTestEvent(EmailGroupChannel.COMPONENT_NAME);
        emailGroupChannel.sendAuditedMessage(event, emailEntity);
    }

    @Override
    public UIComponent getUiComponent() {
        final ConfigField subjectLine = new ConfigField("emailSubjectLine", "Subject Line", FieldType.TEXT_INPUT, false, false, FieldGroup.DEFAULT);
        final ConfigField groupName = new ConfigField("groupName", "Group Name", FieldType.SELECT, true, false, FieldGroup.DEFAULT, getEmailGroups());
        return new UIComponent("Email", "email", "envelope", Arrays.asList(subjectLine, groupName));
    }

    public List<String> getEmailGroups() {
        // TODO we currently query the hub to get the groups, change this when the group DB table is introduced
        try {
            return blackDuckDataActions.getBlackDuckGroups()
                    .stream()
                    .map(blackduckGroup -> blackduckGroup.getName())
                    .collect(Collectors.toList());
        } catch (final IntegrationException e) {
            logger.error("Error retrieving email groups");
            e.printStackTrace();
        }

        return Arrays.asList();
    }

}
