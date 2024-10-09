package com.blackduck.integration.alert.channel.jira.server.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.descriptor.JiraServerChannelKey;
import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.blackduck.integration.alert.common.descriptor.config.ConcreteGlobalConfigExistsValidator;

@Component
public class JiraServerGlobalConfigExistsValidator implements ConcreteGlobalConfigExistsValidator {
    private final JiraServerChannelKey jiraServerChannelKey;
    private final JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor;

    @Autowired
    public JiraServerGlobalConfigExistsValidator(
        JiraServerChannelKey jiraServerChannelKey,
        JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor
    ) {
        this.jiraServerChannelKey = jiraServerChannelKey;
        this.jiraServerGlobalConfigAccessor = jiraServerGlobalConfigAccessor;
    }

    @Override
    public boolean exists() {
        return jiraServerGlobalConfigAccessor.getConfigurationCount() > 0;
    }

    @Override
    public DescriptorKey getDescriptorKey() {
        return jiraServerChannelKey;
    }
}
