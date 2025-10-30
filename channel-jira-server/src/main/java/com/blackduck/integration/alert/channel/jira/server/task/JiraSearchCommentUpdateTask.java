package com.blackduck.integration.alert.channel.jira.server.task;

import com.blackduck.integration.alert.api.channel.jira.JiraConstants;
import com.blackduck.integration.alert.api.channel.jira.distribution.search.JiraIssuePropertyKeys;
import com.blackduck.integration.alert.api.channel.jira.distribution.search.SearchCommentCreator;
import com.blackduck.integration.alert.api.channel.jira.lifecycle.JiraTask;
import com.blackduck.integration.alert.api.task.ScheduledTask;
import com.blackduck.integration.alert.api.task.TaskManager;
import com.blackduck.integration.alert.channel.jira.server.JiraServerProperties;
import com.blackduck.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.model.response.IssuePropertyResponseModel;
import com.blackduck.integration.jira.common.rest.service.IssuePropertyService;
import com.blackduck.integration.jira.common.server.model.IssueCommentRequestModel;
import com.blackduck.integration.jira.common.server.model.IssueSearchIssueComponent;
import com.blackduck.integration.jira.common.server.model.IssueSearchResponseModel;
import com.blackduck.integration.jira.common.server.service.IssueSearchService;
import com.blackduck.integration.jira.common.server.service.IssueService;
import com.blackduck.integration.jira.common.server.service.JiraServerServiceFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class JiraSearchCommentUpdateTask extends JiraTask {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final JiraServerPropertiesFactory jiraPropertiesFactory;

    public JiraSearchCommentUpdateTask(TaskScheduler taskScheduler, TaskManager taskManager, JiraServerPropertiesFactory jiraPropertiesFactory, Gson gson, String configId, String configName, String taskNameSuffix) {
        super(taskScheduler, taskManager, configId, configName, taskNameSuffix, gson);
        this.jiraPropertiesFactory = jiraPropertiesFactory;
    }

    @Override
    public void runTask() {
        logger.info("Jira Server search comment task started.");
        UUID configId = UUID.fromString(getConfigId());
        try {
            JiraServerProperties jiraProperties = jiraPropertiesFactory.createJiraProperties(configId);
            JiraServerServiceFactory serviceFactory = jiraProperties.createJiraServicesServerFactory(logger,getGson());
            IssueSearchService issueSearchService = serviceFactory.createIssueSearchService();
            IssuePropertyService issuePropertyService = serviceFactory.createIssuePropertyService();
            IssueSearchResponseModel responseModel = issueSearchService.queryForIssuePage(JiraConstants.createCommentMigrationJQL(), 0, JQL_QUERY_MAX_RESULTS);
            IssueService issueService = serviceFactory.createIssueService();

            int totalIssues = responseModel.getTotal();
            boolean foundIssues = totalIssues > 0;

            if(foundIssues) {
                List<String> issueKeys = responseModel.getIssues().stream()
                        .map(IssueSearchIssueComponent::getKey)
                        .toList();
                ExecutorService executorService = getExecutorService();
                for (String issueKey : issueKeys) {
                    executorService.submit(createUpdateRunnable(issueKey, issuePropertyService, issueService));
                }
                executorService.shutdown();
                boolean success = executorService.awaitTermination(1, TimeUnit.MINUTES);
                if (success) {
                    logger.info("Jira search key comment update task remaining issues {} ", totalIssues);
                } else {
                    logger.info("Jira search key comment update task timed out updating issues; will resume with the next iteration.");
                }
            } else {

                // unschedule the task because the query returned no results
                unScheduleTask();
            }
        } catch (IntegrationException e) {
            logger.error("Error getting Jira Server Configuration.", e);
        } catch (InterruptedException e) {
            logger.error("Error updating Jira Server issues with new search key comment.", e);
            Thread.currentThread().interrupt();
        }

        logger.info("Jira Server search comment task ended.");
    }

    private Runnable createUpdateRunnable(String issueKey, IssuePropertyService issuePropertyService, IssueService issueService) {
        return () -> {
            Optional<IssuePropertyResponseModel> propertyValue = getPropertyValue(issueKey, issuePropertyService, JiraConstants.JIRA_ISSUE_PROPERTY_KEY);
            if (propertyValue.isEmpty()) {
                // try the old key
                propertyValue = getPropertyValue(issueKey, issuePropertyService, JiraConstants.JIRA_ISSUE_PROPERTY_OLD_KEY);
            }
            if(propertyValue.isPresent()) {
                try {
                    JsonObject jsonObject = propertyValue.get().getValue();
                    // projectName, projectVersionName, and componentName should all be populated but since an optional is returned use orElse(null)
                    String provider = getPropertyValue(jsonObject, JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_PROVIDER).orElse(null);
                    String providerUrl = getPropertyValue(jsonObject, JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_PROVIDER_URL).orElse(null);
                    String projectLabel = getPropertyValue(jsonObject, JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_PROJECT_LABEL).orElse(null);
                    String projectName = getPropertyValue(jsonObject, JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_PROJECT_NAME).orElse(null);
                    String projectVersionLabel = getPropertyValue(jsonObject, JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_PROJECT_VERSION_LABEL).orElse(null);
                    String projectVersionName = getPropertyValue(jsonObject, JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_PROJECT_VERSION_NAME).orElse(null);
                    String componentLabel = getPropertyValue(jsonObject, JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_COMPONENT_LABEL).orElse(null);
                    String componentName = getPropertyValue(jsonObject, JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_COMPONENT_VALUE).orElse(null);
                    String componentVersionLabel = getPropertyValue(jsonObject, JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_SUB_COMPONENT_NAME).orElse(null);
                    String componentVersionName = getPropertyValue(jsonObject, JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_SUB_COMPONENT_VALUE).orElse(null);
                    String category = getPropertyValue(jsonObject, JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_CATEGORY).orElse(null);
                    String policyName = getPropertyValue(jsonObject, JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_ADDITIONAL_KEY).orElse(null);

                    String commentString = SearchCommentCreator.createSearchComment(provider, projectName, projectVersionName, componentName, componentVersionName, category, policyName);
                    IssueCommentRequestModel comment = new IssueCommentRequestModel(issueKey, commentString);
                    issueService.addComment(comment);

                    // mark the issue as migrated
                    JsonObject migratedProperty = new JsonObject();
                    migratedProperty.addProperty(JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_PROVIDER, provider);
                    migratedProperty.addProperty(JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_PROVIDER_URL, providerUrl);
                    migratedProperty.addProperty(JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_PROJECT_LABEL, projectLabel);
                    migratedProperty.addProperty(JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_PROJECT_NAME, projectName);
                    migratedProperty.addProperty(JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_PROJECT_VERSION_LABEL, projectVersionLabel);
                    migratedProperty.addProperty(JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_PROJECT_VERSION_NAME, projectVersionName);
                    migratedProperty.addProperty(JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_COMPONENT_LABEL, componentLabel);
                    migratedProperty.addProperty(JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_COMPONENT_VALUE, componentName);
                    migratedProperty.addProperty(JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_SUB_COMPONENT_NAME, componentVersionLabel);
                    migratedProperty.addProperty(JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_SUB_COMPONENT_VALUE, componentVersionName);
                    migratedProperty.addProperty(JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_CATEGORY, category);
                    migratedProperty.addProperty(JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_ADDITIONAL_KEY, policyName);
                    migratedProperty.addProperty(JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_OBJECT_KEY_ALERT_9_MIGRATED, "true");
                    String migratedPropertyValue = getGson().toJson(migratedProperty);
                    issuePropertyService.setProperty(issueKey, JiraConstants.JIRA_ISSUE_PROPERTY_KEY, migratedPropertyValue);
                } catch (IntegrationException ex) {
                    logger.debug("Error updating issue {} with search key comment.", issueKey);
                    logger.debug("Caused by: ", ex);
                }
            }
        };
    }

    public Optional<String> getPropertyValue(JsonObject jsonObject, String propertyKey) {
        Optional<String> value = Optional.empty();

        if(jsonObject.has(propertyKey)) {
            value = Optional.of(jsonObject.get(propertyKey).getAsString());
        }

        return value;
    }


    protected Optional<IssuePropertyResponseModel> getPropertyValue(String issueKey, IssuePropertyService issuePropertyService, String propertyKey) {
        // empty property value
        Optional<IssuePropertyResponseModel> responseModel = Optional.empty();
        try {
            responseModel = Optional.ofNullable(issuePropertyService.getProperty(issueKey, propertyKey));
        } catch (IntegrationException ex) {
            logger.debug("Error finding issue property {} for issue: {} cause: {}", propertyKey, issueKey, ex.getMessage());
            logger.debug("Caused by: ", ex);
        }
        return responseModel;
    }

    @Override
    public String scheduleCronExpression() {
        return ScheduledTask.EVERY_30_SECONDS_CRON_EXPRESSION;
    }
}
