/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blackduck.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.blackduck.integration.alert.common.rest.api.AbstractFunctionController;

/**
 * @deprecated This class is unused and part of the old Jira Server REST API.
 * Deprecated in 7.x, To be removed in 9.0.0.
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
