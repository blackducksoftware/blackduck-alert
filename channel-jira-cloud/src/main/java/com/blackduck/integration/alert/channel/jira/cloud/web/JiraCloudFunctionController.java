/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blackduck.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.blackduck.integration.alert.common.rest.api.AbstractFunctionController;

@RestController
@RequestMapping(JiraCloudFunctionController.JIRA_CLOUD_FUNCTION_URL)
public class JiraCloudFunctionController extends AbstractFunctionController<String> {
    public static final String JIRA_CLOUD_FUNCTION_URL = AbstractFunctionController.API_FUNCTION_URL + "/" + JiraCloudDescriptor.KEY_JIRA_CONFIGURE_PLUGIN;

    @Autowired
    public JiraCloudFunctionController(JiraCloudCustomFunctionAction functionAction) {
        super(functionAction);
    }
}
