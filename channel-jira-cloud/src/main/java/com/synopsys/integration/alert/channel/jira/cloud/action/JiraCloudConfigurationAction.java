/*
 * channel-jira-cloud
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ConfigurationAction;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

@Component
public class JiraCloudConfigurationAction extends ConfigurationAction {
    @Autowired
    public JiraCloudConfigurationAction(JiraCloudGlobalFieldModelTestAction globalTestAction) {
        super(ChannelKeys.JIRA_CLOUD);
        addGlobalTestAction(globalTestAction);
    }

}
