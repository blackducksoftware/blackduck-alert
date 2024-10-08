/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.web;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blackduck.integration.alert.channel.jira.server.action.JiraServerGlobalCrudActions;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.channel.jira.server.action.JiraServerGlobalTestAction;
import com.blackduck.integration.alert.channel.jira.server.action.JiraServerGlobalValidationAction;
import com.blackduck.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.api.ReadPageController;
import com.synopsys.integration.alert.common.rest.api.StaticConfigResourceController;
import com.synopsys.integration.alert.common.rest.api.ValidateController;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;

@RestController
@RequestMapping(AlertRestConstants.JIRA_SERVER_CONFIGURATION_PATH)
public class JiraServerGlobalConfigController implements StaticConfigResourceController<JiraServerGlobalConfigModel>, ValidateController<JiraServerGlobalConfigModel>, ReadPageController<AlertPagedModel<JiraServerGlobalConfigModel>> {
    private final JiraServerGlobalValidationAction jiraServerGlobalValidationAction;
    private final JiraServerGlobalTestAction jiraServerGlobalTestAction;
    private final JiraServerGlobalCrudActions configActions;
    private final JiraServerInstallPluginAction jiraServerInstallPluginAction;

    @Autowired
    public JiraServerGlobalConfigController(
        JiraServerGlobalValidationAction jiraServerGlobalValidationAction,
        JiraServerGlobalTestAction jiraServerGlobalTestAction,
        JiraServerGlobalCrudActions configActions,
        JiraServerInstallPluginAction jiraServerInstallPluginAction
    ) {
        this.jiraServerGlobalValidationAction = jiraServerGlobalValidationAction;
        this.jiraServerGlobalTestAction = jiraServerGlobalTestAction;
        this.configActions = configActions;
        this.jiraServerInstallPluginAction = jiraServerInstallPluginAction;
    }

    @Override
    public JiraServerGlobalConfigModel getOne(UUID id) {
        return ResponseFactory.createContentResponseFromAction(configActions.getOne(id));
    }

    @Override
    public AlertPagedModel<JiraServerGlobalConfigModel> getPage(
        Integer pageNumber,
        Integer pageSize,
        String searchTerm,
        String sortName,
        String sortOrder
    ) {
        return ResponseFactory.createContentResponseFromAction(configActions.getPaged(pageNumber, pageSize, searchTerm, sortName, sortOrder));
    }

    @Override
    public JiraServerGlobalConfigModel create(JiraServerGlobalConfigModel resource) {
        return ResponseFactory.createContentResponseFromAction(configActions.create(resource));
    }

    @Override
    public void update(UUID id, JiraServerGlobalConfigModel resource) {
        ResponseFactory.createContentResponseFromAction(configActions.update(id, resource));
    }

    @Override
    public ValidationResponseModel validate(JiraServerGlobalConfigModel requestBody) {
        return ResponseFactory.createContentResponseFromAction(jiraServerGlobalValidationAction.validate(requestBody));
    }

    @Override
    public void delete(UUID id) {
        ResponseFactory.createContentResponseFromAction(configActions.delete(id));
    }

    @PostMapping("/test")
    public ValidationResponseModel test(@RequestBody JiraServerGlobalConfigModel resource) {
        return ResponseFactory.createContentResponseFromAction(jiraServerGlobalTestAction.testWithPermissionCheck(resource));
    }

    @PostMapping("/install-plugin")
    public ValidationResponseModel installPlugin(@RequestBody JiraServerGlobalConfigModel resource) {
        return ResponseFactory.createContentResponseFromAction(jiraServerInstallPluginAction.installPlugin(resource));
    }

}
