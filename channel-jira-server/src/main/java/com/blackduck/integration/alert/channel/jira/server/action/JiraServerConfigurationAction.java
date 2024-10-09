package com.blackduck.integration.alert.channel.jira.server.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.common.action.ConfigurationAction;

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
