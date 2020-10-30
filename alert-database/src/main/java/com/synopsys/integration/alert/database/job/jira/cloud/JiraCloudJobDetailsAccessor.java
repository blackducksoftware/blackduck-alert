/**
 * alert-database
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
package com.synopsys.integration.alert.database.job.jira.cloud;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;

@Component
public class JiraCloudJobDetailsAccessor {
    private final JiraCloudJobDetailsRepository jiraCloudJobDetailsRepository;

    @Autowired
    public JiraCloudJobDetailsAccessor(JiraCloudJobDetailsRepository jiraCloudJobDetailsRepository) {
        this.jiraCloudJobDetailsRepository = jiraCloudJobDetailsRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public JiraCloudJobDetailsEntity saveJiraCloudJobDetails(UUID jobId, JiraCloudJobDetailsModel jiraCloudJobDetails) {
        JiraCloudJobDetailsEntity jiraCloudJobDetailsToSave = new JiraCloudJobDetailsEntity(
            jobId,
            jiraCloudJobDetails.isAddComments(),
            jiraCloudJobDetails.getIssueCreatorEmail(),
            jiraCloudJobDetails.getProjectNameOrKey(),
            jiraCloudJobDetails.getIssueType(),
            jiraCloudJobDetails.getResolveTransition(),
            jiraCloudJobDetails.getReopenTransition()
        );
        return jiraCloudJobDetailsRepository.save(jiraCloudJobDetailsToSave);
    }

}
