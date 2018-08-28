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
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.EmailGroupChannel;
import com.synopsys.integration.alert.common.descriptor.config.UIComponent;
import com.synopsys.integration.alert.common.descriptor.config.UIConfig;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.web.provider.blackduck.BlackDuckDataActions;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class EmailDistributionUIConfig extends UIConfig {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final BlackDuckDataActions blackDuckDataActions;

    @Autowired
    public EmailDistributionUIConfig(final BlackDuckDataActions blackDuckDataActions) {
        this.blackDuckDataActions = blackDuckDataActions;
    }

    @Override
    public UIComponent generateUIComponent() {
        return new UIComponent("Email", "email", EmailGroupChannel.COMPONENT_NAME, "envelope", setupFields());
    }

    public List<ConfigField> setupFields() {
        final ConfigField subjectLine = new TextInputConfigField("emailSubjectLine", "Subject Line", false, false);
        final ConfigField groupName = new SelectConfigField("groupName", "Group Name", true, false, getEmailGroups());
        return Arrays.asList(subjectLine, groupName);
    }

    private List<String> getEmailGroups() {
        // TODO we currently query the hub to get the groups, change this when the group DB table is introduced
        try {
            return blackDuckDataActions.getBlackDuckGroups()
                   .stream()
                   .map(blackduckGroup -> blackduckGroup.getName())
                   .collect(Collectors.toList());
        } catch (final IntegrationException ex) {
            logger.error("Error retrieving email groups", ex);
        }

        return Arrays.asList();
    }

}
