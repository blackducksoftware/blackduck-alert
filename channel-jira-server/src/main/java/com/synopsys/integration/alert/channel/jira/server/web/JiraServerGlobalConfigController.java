/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.web;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.channel.jira.server.action.JiraServerGlobalValidationAction;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.api.ReadPageController;
import com.synopsys.integration.alert.common.rest.api.StaticConfigResourceController;
import com.synopsys.integration.alert.common.rest.api.ValidateController;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;

@RestController
@RequestMapping(AlertRestConstants.JIRA_SERVER_CONFIGURATION_PATH)
public class JiraServerGlobalConfigController implements StaticConfigResourceController<JiraServerGlobalConfigModel>, ValidateController<JiraServerGlobalConfigModel>, ReadPageController<AlertPagedModel<JiraServerGlobalConfigModel>> {
    private final JiraServerGlobalValidationAction jiraServerGlobalValidationAction;
    private final JiraServerGlobalTestActions jiraServerGlobalTestActions;
    private final JiraServerGlobalConfigActions jiraServerGlobalConfigActions;
    private final JiraServerDisablePluginAction jiraServerDisablePluginAction;

    @Autowired
    public JiraServerGlobalConfigController(
        JiraServerGlobalValidationAction jiraServerGlobalValidationAction, JiraServerGlobalTestActions jiraServerGlobalTestActions, JiraServerGlobalConfigActions jiraServerGlobalConfigActions,
        JiraServerDisablePluginAction jiraServerDisablePluginAction
    ) {
        this.jiraServerGlobalValidationAction = jiraServerGlobalValidationAction;
        this.jiraServerGlobalTestActions = jiraServerGlobalTestActions;
        this.jiraServerGlobalConfigActions = jiraServerGlobalConfigActions;
        this.jiraServerDisablePluginAction = jiraServerDisablePluginAction;
    }

    @Override
    public JiraServerGlobalConfigModel getOne(UUID id) {
        return ResponseFactory.createContentResponseFromAction(jiraServerGlobalConfigActions.getOne(id));
    }

    @Override
    public AlertPagedModel<JiraServerGlobalConfigModel> getPage(Integer pageNumber, Integer pageSize, String searchTerm) {
        return ResponseFactory.createContentResponseFromAction(jiraServerGlobalConfigActions.getPaged(pageNumber, pageSize));
    }

    @Override
    public JiraServerGlobalConfigModel create(JiraServerGlobalConfigModel resource) {
        return ResponseFactory.createContentResponseFromAction(jiraServerGlobalConfigActions.create(resource));
    }

    @Override
    public void update(UUID id, JiraServerGlobalConfigModel resource) {
        ResponseFactory.createContentResponseFromAction(jiraServerGlobalConfigActions.update(id, resource));
    }

    @Override
    public ValidationResponseModel validate(JiraServerGlobalConfigModel requestBody) {
        return ResponseFactory.createContentResponseFromAction(jiraServerGlobalValidationAction.validate(requestBody));
    }

    @Override
    public void delete(UUID id) {
        ResponseFactory.createContentResponseFromAction(jiraServerGlobalConfigActions.delete(id));
    }

    @PostMapping("/test")
    public ValidationResponseModel test(/*@RequestBody*/ JiraServerGlobalConfigModel resource) {
        return ResponseFactory.createContentResponseFromAction(jiraServerGlobalTestActions.testWithPermissionCheck(resource));
    }

    @PostMapping("disable-plugin")
    public ValidationResponseModel disablePlugin(JiraServerGlobalConfigModel resource) {
        return ResponseFactory.createContentResponseFromAction(jiraServerDisablePluginAction.disablePlugin(resource));
    }

}
