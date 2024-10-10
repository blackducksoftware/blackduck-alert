/*
 * channel-jira-cloud
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.common.action.ConfigurationAction;

@Component
public class JiraCloudConfigurationAction extends ConfigurationAction {
    @Autowired
    public JiraCloudConfigurationAction(JiraCloudGlobalFieldModelTestAction globalTestAction) {
        super(ChannelKeys.JIRA_CLOUD);
        addGlobalTestAction(globalTestAction);
    }

}
