package com.synopsys.integration.alert.common.persistence.model.job.details;

public class AzureBoardsJobDetailsModel extends DistributionJobDetailsModel {
    private final boolean addComments;
    private final String projectNameOrId;
    private final String workItemType;
    private final String workItemCompletedState;
    private final String workItemReopenState;

    public AzureBoardsJobDetailsModel(boolean addComments, String projectNameOrId, String workItemType, String workItemCompletedState, String workItemReopenState) {
        super("channel_azure_boards");
        this.addComments = addComments;
        this.projectNameOrId = projectNameOrId;
        this.workItemType = workItemType;
        this.workItemCompletedState = workItemCompletedState;
        this.workItemReopenState = workItemReopenState;
    }

    public boolean isAddComments() {
        return addComments;
    }

    public String getProjectNameOrId() {
        return projectNameOrId;
    }

    public String getWorkItemType() {
        return workItemType;
    }

    public String getWorkItemCompletedState() {
        return workItemCompletedState;
    }

    public String getWorkItemReopenState() {
        return workItemReopenState;
    }

}
