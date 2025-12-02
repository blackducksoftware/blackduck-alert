/*
 * blackduck-alert
 *
 * Copyright (c) 2025 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.action;

import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.channel.jira.cloud.task.JiraCloudSchedulingManager;
import com.blackduck.integration.alert.common.action.ApiAction;
import com.blackduck.integration.alert.common.rest.model.FieldModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class JiraCloudApiAction extends ApiAction {
    private JiraCloudSchedulingManager jiraSchedulingManager;

    @Autowired
    public JiraCloudApiAction(JiraCloudSchedulingManager jiraSchedulingManager) {
        this.jiraSchedulingManager = jiraSchedulingManager;
    }

    @Override
    public FieldModel afterSaveAction(FieldModel fieldModel) throws AlertException {
        jiraSchedulingManager.scheduleTasks(fieldModel);
        return super.afterSaveAction(fieldModel);
    }

    @Override
    public FieldModel afterUpdateAction(FieldModel previousFieldModel, FieldModel currentFieldModel) throws AlertException {
        jiraSchedulingManager.scheduleTasks(currentFieldModel);
        return super.afterUpdateAction(previousFieldModel, currentFieldModel);
    }

    @Override
    public void afterDeleteAction(FieldModel fieldModel) throws AlertException {
        jiraSchedulingManager.unscheduleTasks(fieldModel.getId());
        super.afterDeleteAction(fieldModel);
    }
}
