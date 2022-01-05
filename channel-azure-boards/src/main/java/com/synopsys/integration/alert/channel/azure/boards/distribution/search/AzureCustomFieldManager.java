/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.model.AzureArrayResponseModel;
import com.synopsys.integration.azure.boards.common.service.process.AzureProcessService;
import com.synopsys.integration.azure.boards.common.service.process.ProcessFieldRequestModel;
import com.synopsys.integration.azure.boards.common.service.process.ProcessFieldResponseModel;
import com.synopsys.integration.azure.boards.common.service.process.ProcessWorkItemTypeRequestModel;
import com.synopsys.integration.azure.boards.common.service.process.ProcessWorkItemTypesResponseModel;
import com.synopsys.integration.azure.boards.common.service.project.AzureProjectService;
import com.synopsys.integration.azure.boards.common.service.project.ProjectPropertyResponseModel;
import com.synopsys.integration.azure.boards.common.service.project.ProjectWorkItemFieldModel;
import com.synopsys.integration.azure.boards.common.service.project.TeamProjectReferenceResponseModel;

public class AzureCustomFieldManager {
    public static final String ALERT_PROVIDER_KEY_FIELD_NAME = "Alert Provider Key";
    public static final String ALERT_PROVIDER_KEY_FIELD_REFERENCE_NAME = "Custom.AlertProviderKey";
    public static final String ALERT_PROVIDER_KEY_FIELD_DESCRIPTION = "A provider tracking key for Alert";

    public static final String ALERT_TOPIC_KEY_FIELD_NAME = "Alert Topic Key";
    public static final String ALERT_TOPIC_KEY_FIELD_REFERENCE_NAME = "Custom.AlertTopicKey";
    public static final String ALERT_TOPIC_KEY_FIELD_DESCRIPTION = "A topic tracking key for Alert";

    public static final String ALERT_SUB_TOPIC_KEY_FIELD_NAME = "Alert SubTopic Key";
    public static final String ALERT_SUB_TOPIC_KEY_FIELD_REFERENCE_NAME = "Custom.AlertSubTopicKey";
    public static final String ALERT_SUB_TOPIC_KEY_FIELD_DESCRIPTION = "A sub-topic tracking key for Alert";

    public static final String ALERT_CATEGORY_KEY_FIELD_NAME = "Alert Category Key";
    public static final String ALERT_CATEGORY_KEY_FIELD_REFERENCE_NAME = "Custom.AlertCategoryKey";
    public static final String ALERT_CATEGORY_KEY_FIELD_DESCRIPTION = "A category tracking key for Alert";

    public static final String ALERT_COMPONENT_KEY_FIELD_NAME = "Alert Component Key";
    public static final String ALERT_COMPONENT_KEY_FIELD_REFERENCE_NAME = "Custom.AlertComponentKey";
    public static final String ALERT_COMPONENT_KEY_FIELD_DESCRIPTION = "A component tracking key for Alert";

    public static final String ALERT_SUB_COMPONENT_KEY_FIELD_NAME = "Alert SubComponent Key";
    public static final String ALERT_SUB_COMPONENT_KEY_FIELD_REFERENCE_NAME = "Custom.AlertSubComponentKey";
    public static final String ALERT_SUB_COMPONENT_KEY_FIELD_DESCRIPTION = "A sub-component tracking key for Alert";

    public static final String ALERT_ADDITIONAL_INFO_KEY_FIELD_NAME = "Alert AdditionalInfo Key";
    public static final String ALERT_ADDITIONAL_INFO_KEY_FIELD_REFERENCE_NAME = "Custom.AlertAdditionalInfoKey";
    public static final String ALERT_ADDITIONAL_INFO_KEY_FIELD_DESCRIPTION = "A tracking key for any additional info needed by Alert";

    private static final List<AzureCustomFieldDescriptor> AZURE_CUSTOM_FIELDS = List.of(
        new AzureCustomFieldDescriptor(ALERT_PROVIDER_KEY_FIELD_NAME, ALERT_PROVIDER_KEY_FIELD_REFERENCE_NAME, ALERT_PROVIDER_KEY_FIELD_DESCRIPTION),
        new AzureCustomFieldDescriptor(ALERT_TOPIC_KEY_FIELD_NAME, ALERT_TOPIC_KEY_FIELD_REFERENCE_NAME, ALERT_TOPIC_KEY_FIELD_DESCRIPTION),
        new AzureCustomFieldDescriptor(ALERT_SUB_TOPIC_KEY_FIELD_NAME, ALERT_SUB_TOPIC_KEY_FIELD_REFERENCE_NAME, ALERT_SUB_TOPIC_KEY_FIELD_DESCRIPTION),
        new AzureCustomFieldDescriptor(ALERT_CATEGORY_KEY_FIELD_NAME, ALERT_CATEGORY_KEY_FIELD_REFERENCE_NAME, ALERT_CATEGORY_KEY_FIELD_DESCRIPTION),
        new AzureCustomFieldDescriptor(ALERT_COMPONENT_KEY_FIELD_NAME, ALERT_COMPONENT_KEY_FIELD_REFERENCE_NAME, ALERT_COMPONENT_KEY_FIELD_DESCRIPTION),
        new AzureCustomFieldDescriptor(ALERT_SUB_COMPONENT_KEY_FIELD_NAME, ALERT_SUB_COMPONENT_KEY_FIELD_REFERENCE_NAME, ALERT_SUB_COMPONENT_KEY_FIELD_DESCRIPTION),
        new AzureCustomFieldDescriptor(ALERT_ADDITIONAL_INFO_KEY_FIELD_NAME, ALERT_ADDITIONAL_INFO_KEY_FIELD_REFERENCE_NAME, ALERT_ADDITIONAL_INFO_KEY_FIELD_DESCRIPTION)
    );

    private static final String UNMODIFIABLE_WORK_ITEM_PREFIX = "Microsoft.VSTS.WorkItemTypes";

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
        List<Future<ProjectWorkItemFieldModel>> projectFieldFindOrCreateHolders = new ArrayList<>(7);
        for (AzureCustomFieldDescriptor fieldDesc : AZURE_CUSTOM_FIELDS) {
            Future<ProjectWorkItemFieldModel> fieldFindOrCreateHolder =
                executorService.submit(() -> findOrCreateAlertCustomProjectField(projectName, fieldDesc.getFieldName(), fieldDesc.getFieldReferenceName(), fieldDesc.getFieldDescription()));
            projectFieldFindOrCreateHolders.add(fieldFindOrCreateHolder);
        }

        TeamProjectReferenceResponseModel project = getProject(projectName);
        String processId = getProjectPropertyValue(project, ProjectPropertyResponseModel.COMMON_PROPERTIES_PROCESS_ID);
        String workItemTypeRefName = getWorkItemTypeRefName(processId, workItemTypeName);

        List<Future<ProcessFieldResponseModel>> processFieldAdditionHolders = new ArrayList<>(7);
        for (Future<ProjectWorkItemFieldModel> projectFieldFuture : projectFieldFindOrCreateHolders) {
            ProjectWorkItemFieldModel projectField = extractFutureResult(projectFieldFuture);
            Future<ProcessFieldResponseModel> processFieldAdditionHolder = executorService.submit(() -> addAlertCustomFieldToProcess(processId, workItemTypeRefName, projectField));
            processFieldAdditionHolders.add(processFieldAdditionHolder);
        }

        for (Future<ProcessFieldResponseModel> processFieldAdditionHolder : processFieldAdditionHolders) {
            extractFutureResult(processFieldAdditionHolder);
        }
    }

    private Optional<ProjectWorkItemFieldModel> getAlertCustomProjectField(String projectName, String fieldReferenceName) {
        try {
            return Optional.of(projectService.getField(organizationName, fieldReferenceName));
        } catch (HttpServiceException e) {
            logger.error(String.format("There was a problem finding the Alert Custom Field (%s) in the Azure project: %s", fieldReferenceName, projectName), e);
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
            throw new AlertException(String.format("There was a problem creating the request to create the Alert Custom Field (%s) in the Azure project: %s", fieldReferenceName, projectName), e);
        } catch (HttpServiceException e) {
            throw new AlertException(String.format("There was a problem creating the Alert Custom Field (%s) in the Azure project: %s", fieldReferenceName, projectName), e);
        }
    }

    private ProcessFieldResponseModel addAlertCustomFieldToProcess(String processId, String workItemTypeRefName, ProjectWorkItemFieldModel projectField) throws AlertException {
        ProcessFieldRequestModel fieldRequestModel = new ProcessFieldRequestModel(false, null, projectField.getReadOnly(), projectField.getReferenceName(), false);
        try {
            return processService.addFieldToWorkItemType(organizationName, processId, workItemTypeRefName, fieldRequestModel);
        } catch (IOException e) {
            throw new AlertException(String.format("There was a problem creating the request to add the Alert Custom Field (%s) to the Azure process with id: %s", projectField.getReferenceName(), processId), e);
        } catch (HttpServiceException e) {
            throw new AlertException(String.format("There was a problem adding the Alert Custom Field (%s) to the Azure process with id: %s", projectField.getReferenceName(), processId), e);
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

    private ProcessWorkItemTypesResponseModel copyWorkItem(String processId, ProcessWorkItemTypesResponseModel workItemType) throws AlertException {
        try {
            return processService.createWorkItemType(organizationName, processId, ProcessWorkItemTypeRequestModel.copyWorkItem(workItemType));
        } catch (IOException | HttpServiceException e) {
            throw new AlertException(
                String.format("There was a problem creating a modifiable work item from '%s' in the Azure process with id: %s. Make sure you are not using a template process.", workItemType.getReferenceName(), processId), e);
        }
    }

    private String getWorkItemTypeRefName(String processId, String workItemTypeName) throws AlertException {
        try {
            AzureArrayResponseModel<ProcessWorkItemTypesResponseModel> processWorkItemTypes = processService.getWorkItemTypes(organizationName, processId);
            ProcessWorkItemTypesResponseModel matchingWorkItemType = processWorkItemTypes.getValue()
                                                                         .stream()
                                                                         .filter(workItemType -> workItemType.getName().equals(workItemTypeName))
                                                                         .findFirst()
                                                                         .orElseThrow(
                                                                             () -> new AlertException(String.format("No work item type '%s' exists for the Azure process with id: '%s'", workItemTypeName, processId)));

            if (matchingWorkItemType.getReferenceName().startsWith(UNMODIFIABLE_WORK_ITEM_PREFIX)) {
                // if the reference name starts with this prefix, we know it is a system default so it can not be modified and fields can not be added to it, so we need to create a "copy" of it
                matchingWorkItemType = copyWorkItem(processId, matchingWorkItemType);
            }
            return matchingWorkItemType.getReferenceName();
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
