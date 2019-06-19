package com.synopsys.integration.alert.channel.jira.web;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.descriptor.JiraDescriptor;
import com.synopsys.integration.alert.common.action.CustomEndpointManager;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.web.controller.ResponseFactory;

@Component
public class JiraCustomEndpoint {
    private final CustomEndpointManager customEndpointManager;
    private final ResponseFactory responseFactory;

    @Autowired
    public JiraCustomEndpoint(final CustomEndpointManager customEndpointManager, final ResponseFactory responseFactory) throws AlertException {
        this.customEndpointManager = customEndpointManager;
        this.responseFactory = responseFactory;

        registerEndpoints();
    }

    public void registerEndpoints() throws AlertException {
        customEndpointManager.registerFunction(JiraDescriptor.KEY_JIRA_CONFIGURE_PLUGIN, this::installJiraPlugin);
    }

    public ResponseEntity<String> installJiraPlugin(final Map<String, FieldValueModel> fieldValueModels) {
        return responseFactory.createMethodNotAllowedResponse("Has not yet been implemented");
    }

}
