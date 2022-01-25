/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.web;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;

// FIXME Implement this class
@Component
public class JiraServerGlobalConfigAction implements IJiraServerGlobalConfigAction {
    @Override
    public ActionResponse<JiraServerGlobalConfigModel> getOne(UUID id) {
        return null;
    }

    @Override
    public ActionResponse<AlertPagedModel<JiraServerGlobalConfigModel>> getPaged(int pageNumber, int pageSize) {
        return null;
    }

    @Override
    public ActionResponse<JiraServerGlobalConfigModel> create(JiraServerGlobalConfigModel resource) {
        return null;
    }

    @Override
    public ActionResponse<JiraServerGlobalConfigModel> update(UUID id, JiraServerGlobalConfigModel resource) {
        return null;
    }

    @Override
    public ActionResponse<JiraServerGlobalConfigModel> delete(UUID id) {
        return null;
    }
}
