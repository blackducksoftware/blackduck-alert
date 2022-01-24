/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.web;

import java.util.UUID;

import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;

public interface JiraServerGlobalConfigActions {

    ActionResponse<JiraServerGlobalConfigModel> getOne(UUID id);

    ActionResponse<AlertPagedModel<JiraServerGlobalConfigModel>> getPaged(int pageNumber, int pageSize);

    ActionResponse<JiraServerGlobalConfigModel> create(JiraServerGlobalConfigModel resource);

    ActionResponse<JiraServerGlobalConfigModel> update(UUID id, JiraServerGlobalConfigModel resource);

    ActionResponse<JiraServerGlobalConfigModel> delete(UUID id);
}
