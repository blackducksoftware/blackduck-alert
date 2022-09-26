/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.synopsys.integration.alert.common.rest.api.AbstractFunctionController;

/**
 * @deprecated This class is unused and part of the old Jira Server REST API. It is set for removal in 8.0.0.
 */
@RestController
@RequestMapping(JiraServerFunctionController.JIRA_SERVER_FUNCTION_URL)
@Deprecated(forRemoval = true)
public class JiraServerFunctionController extends AbstractFunctionController<String> {
    public static final String JIRA_SERVER_FUNCTION_URL = AbstractFunctionController.API_FUNCTION_URL + "/" + JiraServerDescriptor.KEY_JIRA_SERVER_CONFIGURE_PLUGIN;

    @Autowired
    public JiraServerFunctionController(JiraServerCustomFunctionAction functionAction) {
        super(functionAction);
    }
}
