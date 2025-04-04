package com.blackduck.integration.alert.startup.component;

import com.blackduck.integration.alert.api.channel.jira.lifecycle.JiraSchedulingManager;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.blackduck.integration.alert.channel.jira.cloud.task.JiraCloudSchedulingManager;
import com.blackduck.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.blackduck.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.blackduck.integration.alert.channel.jira.server.task.JiraServerSchedulingManager;
import com.blackduck.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;
import com.blackduck.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(65)
public class JiraTaskInitializer extends StartupComponent{
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final JiraServerSchedulingManager jiraServerSchedulingManager;
    private final JiraCloudSchedulingManager jiraCloudSchedulingManager;
    private final JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor;
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
    private final ConfigurationFieldModelConverter configurationFieldModelConverter;

    @Autowired
    public JiraTaskInitializer(JiraServerSchedulingManager jiraServerSchedulingManager, JiraCloudSchedulingManager jiraCloudSchedulingManager, JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor, ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor, ConfigurationFieldModelConverter configurationFieldModelConverter) {
        this.jiraServerSchedulingManager = jiraServerSchedulingManager;
        this.jiraCloudSchedulingManager = jiraCloudSchedulingManager;
        this.jiraServerGlobalConfigAccessor = jiraServerGlobalConfigAccessor;
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
        this.configurationFieldModelConverter = configurationFieldModelConverter;
    }

    @Override
    protected void initialize() {
        startJiraCloudTasks();
        startJiraServerTasks();
    }
    private void startJiraCloudTasks() {
        logger.info("Starting Jira Cloud Tasks");
        List<ConfigurationModel> configurationModelList = configurationModelConfigurationAccessor.getConfigurationsByDescriptorKey(ChannelKeys.JIRA_CLOUD);
        configurationModelList.stream()
                .map(configurationFieldModelConverter::convertToFieldModel)
                .forEach(jiraCloudSchedulingManager::scheduleTasks);
    }

    private void startJiraServerTasks() {
        logger.info("Starting Jira Server Tasks");
        long pagesOfConfiguration = jiraServerGlobalConfigAccessor.getConfigurationCount();
        int currentPage = 0;
        while ( currentPage < pagesOfConfiguration) {
            AlertPagedModel<JiraServerGlobalConfigModel> pageOfData = jiraServerGlobalConfigAccessor.getConfigurationPage(currentPage, 100,null,null,null);
            pageOfData.getModels().forEach(jiraServerSchedulingManager::scheduleTasks);
            currentPage++;
        }
    }
}
