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
package com.synopsys.integration.alert.database.job.jira.server;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.persistence.model.job.details.JiraJobCustomFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.synopsys.integration.alert.database.job.jira.server.custom_field.JiraServerJobCustomFieldEntity;
import com.synopsys.integration.alert.database.job.jira.server.custom_field.JiraServerJobCustomFieldRepository;

@Component
public class JiraServerJobDetailsAccessor {
    private final JiraServerJobDetailsRepository jiraServerJobDetailsRepository;
    private final JiraServerJobCustomFieldRepository jiraServerJobCustomFieldRepository;

    @Autowired
    public JiraServerJobDetailsAccessor(JiraServerJobDetailsRepository jiraServerJobDetailsRepository,
        JiraServerJobCustomFieldRepository jiraServerJobCustomFieldRepository) {
        this.jiraServerJobDetailsRepository = jiraServerJobDetailsRepository;
        this.jiraServerJobCustomFieldRepository = jiraServerJobCustomFieldRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public JiraServerJobDetailsEntity saveJiraServerJobDetails(UUID jobId, JiraServerJobDetailsModel jiraServerJobDetails) {
        JiraServerJobDetailsEntity jiraServerJobDetailsToSave = new JiraServerJobDetailsEntity(
            jobId,
            jiraServerJobDetails.isAddComments(),
            jiraServerJobDetails.getIssueCreatorUsername(),
            jiraServerJobDetails.getProjectNameOrKey(),
            jiraServerJobDetails.getIssueType(),
            jiraServerJobDetails.getResolveTransition(),
            jiraServerJobDetails.getReopenTransition()
        );
        JiraServerJobDetailsEntity savedJobDetails = jiraServerJobDetailsRepository.save(jiraServerJobDetailsToSave);

        List<JiraServerJobCustomFieldEntity> customFieldsToSave = jiraServerJobDetails.getCustomFields()
                                                                      .stream()
                                                                      .map(model -> new JiraServerJobCustomFieldEntity(savedJobDetails.getJobId(), model.getFieldName(), model.getFieldValue()))
                                                                      .collect(Collectors.toList());
        if (!customFieldsToSave.isEmpty()) {
            List<JiraServerJobCustomFieldEntity> savedJobCustomFields = jiraServerJobCustomFieldRepository.saveAll(customFieldsToSave);
            savedJobDetails.setJobCustomFields(savedJobCustomFields);
        }
        return savedJobDetails;
    }

    public List<JiraJobCustomFieldModel> retrieveCustomFieldsForJob(UUID jobId) {
        return jiraServerJobCustomFieldRepository.findByJobId(jobId)
                   .stream()
                   .map(customFieldEntity -> new JiraJobCustomFieldModel(customFieldEntity.getFieldName(), customFieldEntity.getFieldValue()))
                   .collect(Collectors.toList());
    }

}
