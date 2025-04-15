package com.blackduck.integration.alert.api.channel.jira.lifecycle;

import com.blackduck.integration.alert.api.channel.jira.JiraConstants;
import com.blackduck.integration.alert.api.channel.jira.JiraIssueSearchProperties;
import com.blackduck.integration.alert.api.task.ScheduledTask;
import com.blackduck.integration.alert.api.task.TaskManager;
import com.blackduck.integration.alert.api.task.TaskMetaData;
import com.blackduck.integration.alert.api.task.TaskMetaDataProperty;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.model.response.IssuePropertyResponseModel;
import com.blackduck.integration.jira.common.rest.service.IssuePropertyService;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class JiraTask extends ScheduledTask {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    // find tickets created by alert first:
    // 1. A summary that starts with "Alert - Black Duck"
    // 2. A summary that isn't an Alert test message and have a comment "This issue was automatically created by Alert."
    // 3. Then check if the new property key exists on that issue. Only works because the new property key is indexed with the new plugin.
    protected static final String JQL_QUERY_FOR_ISSUE_PROPERTY_MIGRATION = String.format("(summary ~ \"Alert - Black Duck\" OR (summary !~ \"Alert Test Message\" AND comment ~ \"This issue was automatically created by Alert.\")) AND issue.property[%s].topicName IS EMPTY ORDER BY created DESC", JiraConstants.JIRA_ISSUE_PROPERTY_KEY);
    protected static final int JQL_QUERY_MAX_RESULTS = 100;
    protected static final JiraIssueSearchProperties EMPTY_SEARCH_PROPERTIES = new JiraIssueSearchProperties(StringUtils.EMPTY,
            StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
            StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY);

    public static String createProjectSpecificQuery(String projectName) {
        String projectQueryValue = String.format("\"%s\"", projectName);
        return String.format("project = %s AND issue.property[%s].topicName IS EMPTY ORDER BY created DESC", projectQueryValue, JiraConstants.JIRA_ISSUE_PROPERTY_KEY);
    }


    private final TaskManager taskManager;
    private final String taskName;
    private final String configId;
    private final String configName;
    private final Gson gson;

    protected JiraTask(TaskScheduler taskScheduler, TaskManager taskManager, String configId, String configName, String taskNameSuffix, Gson gson) {
        super(taskScheduler);
        this.taskManager = taskManager;
        this.configId = configId;
        this.configName = configName;
        this.taskName = computeTaskName(taskNameSuffix);
        this.gson = gson;
    }

    @Override
    public final void runTask() {
        executeTaskImplementation();
    }

    protected abstract void executeTaskImplementation();

    protected void updateIssues(List<String> issueKeys,Integer totalIssues, IssuePropertyService issuePropertyService) throws InterruptedException {
        ExecutorService executorService = getExecutorService();
        for (String issueKey : issueKeys) {
            executorService.submit(() -> setIssueProperty(issueKey, issuePropertyService));
        }
        executorService.shutdown();
        boolean success = executorService.awaitTermination(1, TimeUnit.MINUTES);
        if (success) {
            logger.info("Jira property migrator task remaining issues {} ", totalIssues);
        } else {
            logger.info("Jira property migrator task timed out updating issues; will resume with the next iteration.");
        }
    }

    protected void setIssueProperty(String issueKey, IssuePropertyService issuePropertyService) {
        try {
            String propertyValue = getCurrentPropertyValue(issueKey, issuePropertyService);
            issuePropertyService.setProperty(issueKey, JiraConstants.JIRA_ISSUE_PROPERTY_KEY, propertyValue);
        } catch (IntegrationException ex) {
            logger.error("Error migrating issue property for issue: {} cause: {}", issueKey, ex.getMessage());
            logger.debug("Caused by: ", ex);
        }
    }

    protected String getCurrentPropertyValue(String issueKey, IssuePropertyService issuePropertyService) {
        // empty property value
        String jsonPropertyValue = gson.toJson(EMPTY_SEARCH_PROPERTIES);
        try {
            IssuePropertyResponseModel issuePropertyResponse = issuePropertyService.getProperty(issueKey, JiraConstants.JIRA_ISSUE_PROPERTY_OLD_KEY);
            jsonPropertyValue = gson.toJson(issuePropertyResponse.getValue());
        } catch (IntegrationException ex) {
            logger.debug("Error old issue property for issue: {} cause: {}", issueKey, ex.getMessage());
            logger.debug("Caused by: ", ex);
        }
        return jsonPropertyValue;
    }

    @Override
    public String getTaskName() {
        return taskName;
    }

    @Override
    public TaskMetaData createTaskMetaData() {
        String fullyQualifiedName = ScheduledTask.computeFullyQualifiedName(getClass());
        String nextRunTime = getFormatedNextRunTime().orElse("");
        TaskMetaDataProperty configNameProperty = new TaskMetaDataProperty("configurationName", "Configuration Name", getConfigName());
        List<TaskMetaDataProperty> properties = List.of(configNameProperty);
        return new TaskMetaData(getTaskName(), getClass().getSimpleName(), fullyQualifiedName, nextRunTime, properties);
    }

    public String getConfigId() {
        return configId;
    }

    public String getConfigName() {
        return configName;
    }

    protected Gson getGson() {
        return gson;
    }

    protected ExecutorService getExecutorService() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public void unScheduleTask() {
        taskManager.unScheduleTask(taskName);
        taskManager.unregisterTask(taskName);
    }

    private String computeTaskName(String taskNameSuffix) {
        String superTaskName = ScheduledTask.computeTaskName(getClass());
        return String.format("%s::%s[id:%s]", superTaskName, taskNameSuffix,getConfigId());
    }
}
