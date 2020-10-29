package com.synopsys.integration.alert.common.persistence.model.job.details;

public class JiraServerJobDetailsModel extends DistributionJobDetailsModel {
    private final boolean addComments;
    private final String issueCreatorUsername;
    private final String projectNameOrKey;
    private final String issueType;
    private final String resolveTransition;
    private final String reopenTransition;

    public JiraServerJobDetailsModel(boolean addComments, String issueCreatorUsername, String projectNameOrKey, String issueType, String resolveTransition, String reopenTransition) {
        super("channel_jira_server");
        this.addComments = addComments;
        this.issueCreatorUsername = issueCreatorUsername;
        this.projectNameOrKey = projectNameOrKey;
        this.issueType = issueType;
        this.resolveTransition = resolveTransition;
        this.reopenTransition = reopenTransition;
    }

    public boolean isAddComments() {
        return addComments;
    }

    public String getIssueCreatorUsername() {
        return issueCreatorUsername;
    }

    public String getProjectNameOrKey() {
        return projectNameOrKey;
    }

    public String getIssueType() {
        return issueType;
    }

    public String getResolveTransition() {
        return resolveTransition;
    }

    public String getReopenTransition() {
        return reopenTransition;
    }

}
