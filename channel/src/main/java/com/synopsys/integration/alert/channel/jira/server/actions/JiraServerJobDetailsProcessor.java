/**
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.jira.server.actions;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.common.actions.JiraJobDetailsProcessor;
import com.synopsys.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;

@Component
public class JiraServerJobDetailsProcessor extends JiraJobDetailsProcessor {

    @Autowired
    public JiraServerJobDetailsProcessor(Gson gson) {
        super(gson);
    }

    @Override
    protected DistributionJobDetailsModel convertToChannelJobDetails(Map<String, ConfigurationFieldModel> configuredFieldsMap) {
        return new JiraServerJobDetailsModel(
            extractFieldValue(JiraServerDescriptor.KEY_ADD_COMMENTS, configuredFieldsMap).map(Boolean::valueOf).orElse(false),
            extractFieldValueOrEmptyString(JiraServerDescriptor.KEY_ISSUE_CREATOR, configuredFieldsMap),
            extractFieldValueOrEmptyString(JiraServerDescriptor.KEY_JIRA_PROJECT_NAME, configuredFieldsMap),
            extractFieldValueOrEmptyString(JiraServerDescriptor.KEY_ISSUE_TYPE, configuredFieldsMap),
            extractFieldValueOrEmptyString(JiraServerDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION, configuredFieldsMap),
            extractFieldValueOrEmptyString(JiraServerDescriptor.KEY_OPEN_WORKFLOW_TRANSITION, configuredFieldsMap),
            extractJiraFieldMappings(JiraServerDescriptor.KEY_FIELD_MAPPING, configuredFieldsMap)
        );
    }
}
