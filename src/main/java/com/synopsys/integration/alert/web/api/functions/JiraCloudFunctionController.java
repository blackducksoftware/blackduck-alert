/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.web.api.functions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.synopsys.integration.alert.channel.jira.cloud.web.JiraCloudCustomEndpoint;

@RestController
@RequestMapping(JiraCloudFunctionController.JIRA_CLOUD_FUNCTION_URL)
public class JiraCloudFunctionController extends AbstractFunctionController {
    public static final String JIRA_CLOUD_FUNCTION_URL = AbstractFunctionController.API_FUNCTION_URL + "/" + JiraCloudDescriptor.KEY_JIRA_CONFIGURE_PLUGIN;

    @Autowired
    public JiraCloudFunctionController(JiraCloudCustomEndpoint functionAction) {
        super(functionAction);
    }
}
