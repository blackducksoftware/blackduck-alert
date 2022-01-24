package com.synopsys.integration.alert.channel.jira.server.web;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;

// FIXME Implement this class
@Component
public class JiraServerGlobalTestAction implements IJiraServerGlobalTestAction {
    @Override
    public ActionResponse<ValidationResponseModel> testWithPermissionCheck(JiraServerGlobalConfigModel resource) {
        return null;
    }
}
