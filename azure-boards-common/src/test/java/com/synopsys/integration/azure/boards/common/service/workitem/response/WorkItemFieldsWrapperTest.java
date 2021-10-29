package com.synopsys.integration.azure.boards.common.service.workitem.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.synopsys.integration.azure.boards.common.util.AzureFieldsExtractor;

public class WorkItemFieldsWrapperTest {
    private final AzureFieldsExtractor azureFieldsExtractor = new AzureFieldsExtractor(new Gson());
    private final JsonObject jsonObject = new JsonObject();

    @Test
    public void getTeamProjectTest() {
        String teamProjectName = "TeamProject";
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(WorkItemResponseFields.System_TeamProject.getFieldName(), new JsonPrimitive(teamProjectName));

        WorkItemFieldsWrapper workItemFieldsWrapper = new WorkItemFieldsWrapper(azureFieldsExtractor, jsonObject);
        Optional<String> teamProject = workItemFieldsWrapper.getTeamProject();
        assertTrue(teamProject.isPresent());
        assertEquals(teamProjectName, teamProject.get());
    }

    @Test
    public void getWorkItemTypeTest() {
        String workItemTypeName = "WorkItemType";
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(WorkItemResponseFields.System_WorkItemType.getFieldName(), new JsonPrimitive(workItemTypeName));

        WorkItemFieldsWrapper workItemFieldsWrapper = new WorkItemFieldsWrapper(azureFieldsExtractor, jsonObject);
        Optional<String> workItemType = workItemFieldsWrapper.getWorkItemType();
        assertTrue(workItemType.isPresent());
        assertEquals(workItemTypeName, workItemType.get());
    }

    @Test
    public void getStateTest() {
        String stateName = "State";
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(WorkItemResponseFields.System_State.getFieldName(), new JsonPrimitive(stateName));

        WorkItemFieldsWrapper workItemFieldsWrapper = new WorkItemFieldsWrapper(azureFieldsExtractor, jsonObject);
        Optional<String> state = workItemFieldsWrapper.getState();
        assertTrue(state.isPresent());
        assertEquals(stateName, state.get());
    }

    @Test
    public void getTitleTest() {
        String titleName = "Title";
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(WorkItemResponseFields.System_Title.getFieldName(), new JsonPrimitive(titleName));

        WorkItemFieldsWrapper workItemFieldsWrapper = new WorkItemFieldsWrapper(azureFieldsExtractor, jsonObject);
        Optional<String> title = workItemFieldsWrapper.getTitle();
        assertTrue(title.isPresent());
        assertEquals(titleName, title.get());
    }

    @Test
    public void getDescriptionTest() {
        String descriptionName = "Description";
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(WorkItemResponseFields.System_Description.getFieldName(), new JsonPrimitive(descriptionName));

        WorkItemFieldsWrapper workItemFieldsWrapper = new WorkItemFieldsWrapper(azureFieldsExtractor, jsonObject);
        Optional<String> description = workItemFieldsWrapper.getDescription();
        assertTrue(description.isPresent());
        assertEquals(descriptionName, description.get());
    }

    @Test
    public void getFieldTest() {
        String taskTypeValue = "VSTS_CMMI_TaskType";
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(WorkItemResponseFields.Microsoft_VSTS_CMMI_TaskType.getFieldName(), new JsonPrimitive(taskTypeValue));

        WorkItemFieldsWrapper workItemFieldsWrapper = new WorkItemFieldsWrapper(azureFieldsExtractor, jsonObject);
        Optional<String> taskType = workItemFieldsWrapper.getField(WorkItemResponseFields.Microsoft_VSTS_CMMI_TaskType);
        assertTrue(taskType.isPresent());
        assertEquals(taskTypeValue, taskType.get());
    }

    @Test
    public void getFieldEmptyTest() {
        JsonObject jsonObject = new JsonObject();
        WorkItemFieldsWrapper workItemFieldsWrapper = new WorkItemFieldsWrapper(azureFieldsExtractor, jsonObject);
        Optional<String> value = workItemFieldsWrapper.getField(WorkItemResponseFields.System_TeamProject);

        assertTrue(value.isEmpty());

    }
}
