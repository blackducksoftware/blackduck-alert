package com.blackduck.integration.alert.channel.jira.cloud.action;

import com.blackduck.integration.alert.api.channel.jira.lifecycle.JiraSchedulingManager;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.common.action.ApiAction;
import com.blackduck.integration.alert.common.rest.model.FieldModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JiraCloudApiAction extends ApiAction {
    private JiraSchedulingManager jiraSchedulingManager;

    @Autowired
    public JiraCloudApiAction(JiraSchedulingManager jiraSchedulingManager) {
        this.jiraSchedulingManager = jiraSchedulingManager;
    }

    @Override
    public FieldModel afterSaveAction(FieldModel fieldModel) throws AlertException {
        return super.afterSaveAction(fieldModel);
    }

    @Override
    public FieldModel afterUpdateAction(FieldModel previousFieldModel, FieldModel currentFieldModel) throws AlertException {
        return super.afterUpdateAction(previousFieldModel, currentFieldModel);
    }

    @Override
    public void afterDeleteAction(FieldModel fieldModel) throws AlertException {
        super.afterDeleteAction(fieldModel);
    }
}
