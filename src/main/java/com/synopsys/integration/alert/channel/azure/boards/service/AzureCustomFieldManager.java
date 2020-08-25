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
package com.synopsys.integration.alert.channel.azure.boards.service;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.model.AzureArrayResponseModel;
import com.synopsys.integration.azure.boards.common.service.process.AzureProcessService;
import com.synopsys.integration.azure.boards.common.service.process.ProcessFieldRequestModel;
import com.synopsys.integration.azure.boards.common.service.process.ProcessFieldResponseModel;
import com.synopsys.integration.azure.boards.common.service.process.ProcessWorkItemTypesResponseModel;
import com.synopsys.integration.azure.boards.common.service.project.AzureProjectService;
import com.synopsys.integration.azure.boards.common.service.project.ProjectPropertyResponseModel;
import com.synopsys.integration.azure.boards.common.service.project.ProjectWorkItemFieldModel;
import com.synopsys.integration.azure.boards.common.service.project.TeamProjectReferenceResponseModel;

public class AzureCustomFieldManager {
    public static final String ALERT_PROVIDER_KEY_FIELD_NAME = "Alert Provider Key";
    public static final String ALERT_PROVIDER_KEY_FIELD_REFERENCE_NAME = "Custom.AlertProviderKey";
    public static final String ALERT_PROVIDER_KEY_FIELD_DESCRIPTION = "A provider-level tracking key for Alert";

    public static final String ALERT_TOPIC_KEY_FIELD_NAME = "Alert Topic Key";
    public static final String ALERT_TOPIC_KEY_FIELD_REFERENCE_NAME = "Custom.AlertTopicKey";
    public static final String ALERT_TOPIC_KEY_FIELD_DESCRIPTION = "A topic-level tracking key for Alert";

    public static final String ALERT_COMPONENT_KEY_FIELD_NAME = "Alert Component Key";
    public static final String ALERT_COMPONENT_KEY_FIELD_REFERENCE_NAME = "Custom.AlertComponentKey";
    public static final String ALERT_COMPONENT_KEY_FIELD_DESCRIPTION = "A component-level tracking key for Alert";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String organizationName;
    private final AzureProjectService projectService;
    private final AzureProcessService processService;
    private final ExecutorService executorService;

    public AzureCustomFieldManager(String organizationName, AzureProjectService projectService, AzureProcessService processService, ExecutorService executorService) {
        this.organizationName = organizationName;
        this.projectService = projectService;
        this.processService = processService;
        this.executorService = executorService;
    }

    public void installCustomFields(String projectName, String workItemTypeName) throws AlertException {
        Future<ProjectWorkItemFieldModel> providerKeyFieldCreationResultHolder =
            executorService.submit(() -> findOrCreateAlertCustomProjectField(projectName, ALERT_PROVIDER_KEY_FIELD_NAME, ALERT_PROVIDER_KEY_FIELD_REFERENCE_NAME, ALERT_PROVIDER_KEY_FIELD_DESCRIPTION));
        Future<ProjectWorkItemFieldModel> topicKeyFieldCreationResultHolder =
            executorService.submit(() -> findOrCreateAlertCustomProjectField(projectName, ALERT_TOPIC_KEY_FIELD_NAME, ALERT_TOPIC_KEY_FIELD_REFERENCE_NAME, ALERT_TOPIC_KEY_FIELD_DESCRIPTION));
        Future<ProjectWorkItemFieldModel> componentKeyFieldCreationResultHolder =
            executorService.submit(() -> findOrCreateAlertCustomProjectField(projectName, ALERT_COMPONENT_KEY_FIELD_NAME, ALERT_COMPONENT_KEY_FIELD_REFERENCE_NAME, ALERT_COMPONENT_KEY_FIELD_DESCRIPTION));

        TeamProjectReferenceResponseModel project = getProject(projectName);
        String processId = getProjectPropertyValue(project, ProjectPropertyResponseModel.COMMON_PROPERTIES_PROCESS_ID);
        String workItemTypeRefName = getWorkItemTypeRefName(processId, workItemTypeName);

        ProjectWorkItemFieldModel providerKeyField = extractFutureResult(providerKeyFieldCreationResultHolder);
        Future<ProcessFieldResponseModel> processProviderFieldKeyResultHolder = executorService.submit(() -> addAlertCustomFieldToProcess(processId, workItemTypeRefName, providerKeyField));

        ProjectWorkItemFieldModel topicKeyField = extractFutureResult(topicKeyFieldCreationResultHolder);
        Future<ProcessFieldResponseModel> processTopicFieldKeyResultHolder = executorService.submit(() -> addAlertCustomFieldToProcess(processId, workItemTypeRefName, topicKeyField));

        ProjectWorkItemFieldModel componentKeyField = extractFutureResult(componentKeyFieldCreationResultHolder);
        Future<ProcessFieldResponseModel> processComponentFieldKeyResultHolder = executorService.submit(() -> addAlertCustomFieldToProcess(processId, workItemTypeRefName, componentKeyField));

        extractFutureResult(processProviderFieldKeyResultHolder);
        extractFutureResult(processTopicFieldKeyResultHolder);
        extractFutureResult(processComponentFieldKeyResultHolder);
    }

    private Optional<ProjectWorkItemFieldModel> getAlertCustomProjectField(String projectName, String fieldReferenceName) {
        try {
            return Optional.of(projectService.getField(organizationName, fieldReferenceName));
        } catch (IOException e) {
            logger.error(String.format("There was a problem creating the request to get the Alert Custom Field in the Azure project: %s", projectName), e);
        } catch (HttpServiceException e) {
            logger.error(String.format("There was a problem finding the Alert Custom Field in the Azure project: %s", projectName), e);
        }
        return Optional.empty();
    }

    private ProjectWorkItemFieldModel findOrCreateAlertCustomProjectField(String projectName, String fieldName, String fieldReferenceName, String fieldDescription) throws AlertException {
        ProjectWorkItemFieldModel fieldRequestModel = ProjectWorkItemFieldModel.workItemStringField(fieldName, fieldReferenceName, fieldDescription);
        Optional<ProjectWorkItemFieldModel> customField = getAlertCustomProjectField(projectName, fieldRequestModel.getReferenceName());
        if (customField.isPresent()) {
            return customField.get();
        }
        // custom field not found so create it
        try {
            return projectService.createProjectField(organizationName, projectName, fieldRequestModel);
        } catch (IOException e) {
            throw new AlertException(String.format("There was a problem creating the request to create the Alert Custom Field in the Azure project: %s", projectName), e);
        } catch (HttpServiceException e) {
            throw new AlertException(String.format("There was a problem creating the Alert Custom Field in the Azure project: %s", projectName), e);
        }
    }

    private ProcessFieldResponseModel addAlertCustomFieldToProcess(String processId, String workItemTypeRefName, ProjectWorkItemFieldModel projectField) throws AlertException {
        ProcessFieldRequestModel fieldRequestModel = new ProcessFieldRequestModel(false, null, projectField.getReadOnly(), projectField.getReferenceName(), false);
        try {
            return processService.addFieldToWorkItemType(organizationName, processId, workItemTypeRefName, fieldRequestModel);
        } catch (IOException e) {
            throw new AlertException(String.format("There was a problem creating the request to add the Alert Custom Field to the Azure process with id: %s", processId), e);
        } catch (HttpServiceException e) {
            throw new AlertException(String.format("There was a problem adding the Alert Custom Field to the Azure process with id: %s", processId), e);
        }
    }

    private TeamProjectReferenceResponseModel getProject(String projectName) throws AlertException {
        AzureArrayResponseModel<TeamProjectReferenceResponseModel> azureProjects;
        try {
            azureProjects = projectService.getProjects(organizationName);
        } catch (HttpServiceException e) {
            throw new AlertException("There was a problem trying to get the Azure projects", e);
        }
        return azureProjects.getValue()
                   .stream()
                   .filter(project -> project.getName().equals(projectName))
                   .findFirst()
                   .orElseThrow(() -> new AlertException(String.format("No Azure project with the name '%s' exists", projectName)));
    }

    private String getProjectPropertyValue(TeamProjectReferenceResponseModel project, String propertyName) throws AlertException {
        try {
            AzureArrayResponseModel<ProjectPropertyResponseModel> projectProperties = projectService.getProjectProperties(organizationName, project.getId());
            return projectProperties.getValue()
                       .stream()
                       .filter(prop -> prop.getName().equals(propertyName))
                       .map(ProjectPropertyResponseModel::getValue)
                       .findFirst()
                       .orElseThrow(() -> new AlertException(String.format("No property '%s' for the Azure project with the name '%s' exists", propertyName, project.getName())));
        } catch (HttpServiceException e) {
            throw new AlertException(String.format("There was a problem trying to get the properties for Azure project: %s", project.getName()), e);
        }
    }

    private String getWorkItemTypeRefName(String processId, String workItemTypeName) throws AlertException {
        try {
            AzureArrayResponseModel<ProcessWorkItemTypesResponseModel> processWorkItemTypes = processService.getWorkItemTypes(organizationName, processId);
            return processWorkItemTypes.getValue()
                       .stream()
                       .filter(workItemType -> workItemType.getName().equals(workItemTypeName))
                       .map(ProcessWorkItemTypesResponseModel::getReferenceName)
                       .findFirst()
                       .orElseThrow(() -> new AlertException(String.format("No work item type '%s' exists for the Azure process with id: '%s'", workItemTypeName, processId)));
        } catch (HttpServiceException e) {
            throw new AlertException(String.format("There was a problem trying to get the work item types for the Azure process with id: %s", processId), e);
        }
    }

    private <T> T extractFutureResult(Future<T> projectFieldCreationResult) throws AlertException {
        try {
            return projectFieldCreationResult.get(10L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            String interruptedMessage = "The thread handling Azure field creation was interrupted";
            logger.warn(interruptedMessage, e);
            Thread.currentThread().interrupt();
            throw new AlertException(interruptedMessage, e);
        } catch (ExecutionException e) {
            // This will already wrap an Alert exception
            throw new AlertException(e);
        } catch (TimeoutException e) {
            throw new AlertException("The request to create the Azure field timed out", e);
        }
    }

}
