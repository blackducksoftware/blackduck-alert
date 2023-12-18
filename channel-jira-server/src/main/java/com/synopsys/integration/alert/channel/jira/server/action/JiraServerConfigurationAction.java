/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ConfigurationAction;
import com.synopsys.integration.alert.api.descriptor.model.ChannelKeys;

/**
 * @deprecated Global configuration actions for Jira Server are now handled through JiraServerGlobalCrudActions
 */
@Component
@Deprecated(forRemoval = true)
public class JiraServerConfigurationAction extends ConfigurationAction {
    @Autowired
    protected JiraServerConfigurationAction(JiraServerGlobalFieldModelTestAction jiraServerGlobalTestAction) {
        super(ChannelKeys.JIRA_SERVER);
        addGlobalTestAction(jiraServerGlobalTestAction);
    }

}
