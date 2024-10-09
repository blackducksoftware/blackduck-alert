package com.blackduck.integration.alert.api.channel.issue.tracker.action;

import java.util.List;

import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;

public class IssueTrackerTestActionFieldStatusCreator {
    private static final String FIELD_PLACEHOLDER = "none";

    //TODO: This addresses that hasErrors is set in the UI but this should be improved upon since it is not associated with a field
    public List<AlertFieldStatus> createWithoutField(String errorMessage) {
        return List.of(AlertFieldStatus.error(FIELD_PLACEHOLDER, errorMessage));
    }

}
