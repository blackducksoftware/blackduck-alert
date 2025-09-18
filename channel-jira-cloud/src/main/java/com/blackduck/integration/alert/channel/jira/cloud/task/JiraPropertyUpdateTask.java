package com.blackduck.integration.alert.channel.jira.cloud.task;

import com.blackduck.integration.alert.api.channel.jira.lifecycle.JiraTask;
import com.blackduck.integration.alert.api.task.ScheduledTask;
import com.blackduck.integration.alert.api.task.TaskManager;
import com.blackduck.integration.alert.channel.jira.cloud.JiraCloudProperties;
import com.blackduck.integration.alert.channel.jira.cloud.JiraCloudPropertiesFactory;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.cloud.model.IssueSearchResponseModel;
import com.blackduck.integration.jira.common.cloud.service.IssueSearchService;
import com.blackduck.integration.jira.common.cloud.service.JiraCloudServiceFactory;
import com.blackduck.integration.jira.common.model.response.IssueResponseModel;
import com.blackduck.integration.jira.common.rest.service.IssuePropertyService;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import java.util.List;
import java.util.Set;

public class JiraPropertyUpdateTask extends JiraTask {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final JiraCloudPropertiesFactory jiraPropertiesFactory;
    private final Set<String> projectNamesOrKeys;

    public JiraPropertyUpdateTask(TaskScheduler taskScheduler, TaskManager taskManager, JiraCloudPropertiesFactory jiraPropertiesFactory, Gson gson, String configId, String configName, String taskNameSuffix, Set<String> projectNamesOrKeys) {
        super(taskScheduler, taskManager, configId, configName, taskNameSuffix, gson);
        this.jiraPropertiesFactory = jiraPropertiesFactory;
        this.projectNamesOrKeys = projectNamesOrKeys;
    }

    @Override
    protected void executeTaskImplementation() {
        logger.info("Jira Cloud property migrator task started.");
        try {
            JiraCloudProperties jiraProperties = jiraPropertiesFactory.createJiraProperties();
            JiraCloudServiceFactory serviceFactory = jiraProperties.createJiraServicesCloudFactory(logger, getGson());
            IssueSearchService issueSearchService = serviceFactory.createIssueSearchService();
            IssuePropertyService issuePropertyService = serviceFactory.createIssuePropertyService();

            IssueSearchResponseModel responseModel = issueSearchService.queryForIssuePage(JQL_QUERY_FOR_ISSUE_PROPERTY_MIGRATION, null, JQL_QUERY_MAX_RESULTS);
            boolean foundIssuesForDefaultQuery = updateIssues(responseModel, issuePropertyService);

            if(!foundIssuesForDefaultQuery) {
                if (!projectNamesOrKeys.isEmpty()) {
                    String projectName = projectNamesOrKeys.stream().findFirst().orElse("");
                    if(StringUtils.isNotBlank(projectName)) {
                        logger.info("Querying issues for {} remaining project(s).  Querying issues for project: {}. ", projectNamesOrKeys.size(), projectName);
                        logger.debug("Remaining projects: {}", projectNamesOrKeys);
                        responseModel = issueSearchService.queryForIssuePage(createProjectSpecificQuery(projectName), null, JQL_QUERY_MAX_RESULTS);
                        boolean foundIssuesForProject = updateIssues(responseModel, issuePropertyService);
                        if (!foundIssuesForProject) {
                            // remove the key to no longer query for that project.
                            projectNamesOrKeys.remove(projectName);
                        }
                    }
                } else {
                    // unschedule the task because the default query returned no results and there are no specific
                    // projects to query for.
                    unScheduleTask();
                }
            }
            logger.info("Jira Cloud property migrator task ended.");
        } catch (IntegrationException e) {
            logger.error("Error getting Jira Cloud Configuration.", e);
        } catch (InterruptedException e) {
            logger.error("Error updating Jira Cloud issues with new property.", e);
            Thread.currentThread().interrupt();
        }
    }

    private boolean updateIssues(IssueSearchResponseModel responseModel, IssuePropertyService issuePropertyService) throws InterruptedException {
        int totalIssues = responseModel.getTotal();
        boolean foundIssues = totalIssues > 0;

        if(foundIssues) {
            List<String> issueKeys = responseModel.getIssues().stream()
                    .map(IssueResponseModel::getKey)
                    .toList();
            updateIssues(issueKeys, totalIssues, issuePropertyService);
        }
        return foundIssues;
    }

    @Override
    public String scheduleCronExpression() {
        return ScheduledTask.EVERY_30_SECONDS_CRON_EXPRESSION;
    }
}
