package com.synopsys.integration.alert.common.persistence.model.job.details;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

public class DistributionJobDetailsModelJsonAdapterTest {
    private final Gson gson = new Gson();
    private final JsonDeserializationContext jsonDeserializationContext = gson::fromJson;
    private final JsonSerializationContext jsonSerializationContext = new JsonSerializationContext() {
        @Override
        public JsonElement serialize(Object o) {
            return gson.toJsonTree(o);
        }

        @Override
        public JsonElement serialize(Object o, Type type) {
            return gson.toJsonTree(o, type);
        }
    };

    // SERIALIZATION TESTS

    @Test
    public void serializeAzureBoardsJobDetailsModelTest() {
        AzureBoardsJobDetailsModel baseModel = new AzureBoardsJobDetailsModel(true, "project name", "task", "none", "alt");
        JsonElement baseJson = gson.toJsonTree(baseModel);
        serializeAndAssert(baseModel, baseJson);
    }

    @Test
    public void serializeEmailJobDetailsModelTest() {
        EmailJobDetailsModel baseModel = new EmailJobDetailsModel("alert subject", false, true, null, List.of("email 1", "email 2"));
        JsonElement baseJson = gson.toJsonTree(baseModel);
        serializeAndAssert(baseModel, baseJson);
    }

    @Test
    public void serializeJiraCloudJobDetailsModelTest() {
        JiraCloudJobDetailsModel baseModel = new JiraCloudJobDetailsModel(true, "unknown", "JIRA-X", "bug", "done", "undone");
        JsonElement baseJson = gson.toJsonTree(baseModel);
        serializeAndAssert(baseModel, baseJson);
    }

    @Test
    public void serializeJiraServerJobDetailsModelTest() {
        JiraServerJobDetailsModel baseModel = new JiraServerJobDetailsModel(true, "user_name01", "JIRA-Y", "other", "finished", "unfinished");
        JsonElement baseJson = gson.toJsonTree(baseModel);
        serializeAndAssert(baseModel, baseJson);
    }

    @Test
    public void serializeMSTeamsJobDetailsModelTest() {
        MSTeamsJobDetailsModel baseModel = new MSTeamsJobDetailsModel("webhook_url");
        JsonElement baseJson = gson.toJsonTree(baseModel);
        serializeAndAssert(baseModel, baseJson);
    }

    @Test
    public void serializeSlackJobDetailsModelTest() {
        SlackJobDetailsModel baseModel = new SlackJobDetailsModel("slack_webhook_url", "a-cool-channel", "Channel Tester");
        JsonElement baseJson = gson.toJsonTree(baseModel);
        serializeAndAssert(baseModel, baseJson);
    }

    @Test
    public void serializeAndVerifyNotEqualTest() {
        MSTeamsJobDetailsModel baseModel1 = new MSTeamsJobDetailsModel("webhook_url");
        MSTeamsJobDetailsModel baseModel2 = new MSTeamsJobDetailsModel("different_webhook_url");

        DistributionJobDetailsModelJsonAdapter jsonAdapter = new DistributionJobDetailsModelJsonAdapter();
        JsonElement json1 = jsonAdapter.serialize(baseModel1, DistributionJobDetailsModel.class, jsonSerializationContext);
        JsonElement json2 = jsonAdapter.serialize(baseModel2, DistributionJobDetailsModel.class, jsonSerializationContext);

        assertNotEquals(json1, json2);
    }

    // DESERIALIZATION TESTS

    @Test
    public void deserializeAzureBoardsJobDetailsModelTest() {
        AzureBoardsJobDetailsModel baseModel = new AzureBoardsJobDetailsModel(true, "project name", "task", "none", "alt");

        DistributionJobDetailsModel deserializedModel = runDeserializerAndAssert(baseModel, DistributionJobDetailsModel::isAzureBoardsDetails);

        AzureBoardsJobDetailsModel jobDetails = deserializedModel.getAsAzureBoardsJobDetails();
        assertEquals(baseModel.isAddComments(), jobDetails.isAddComments());
        assertEquals(baseModel.getProjectNameOrId(), jobDetails.getProjectNameOrId());
        assertEquals(baseModel.getWorkItemType(), jobDetails.getWorkItemType());
        assertEquals(baseModel.getWorkItemCompletedState(), jobDetails.getWorkItemCompletedState());
        assertEquals(baseModel.getWorkItemReopenState(), jobDetails.getWorkItemReopenState());
    }

    @Test
    public void deserializeEmailJobDetailsModelTest() {
        EmailJobDetailsModel baseModel = new EmailJobDetailsModel("alert subject", false, true, null, List.of("email 1", "email 2"));

        DistributionJobDetailsModel deserializedModel = runDeserializerAndAssert(baseModel, DistributionJobDetailsModel::isEmailDetails);

        EmailJobDetailsModel jobDetails = deserializedModel.getAsEmailJobDetails();
        assertEquals(baseModel.getSubjectLine(), jobDetails.getSubjectLine());
        assertEquals(baseModel.isProjectOwnerOnly(), jobDetails.isProjectOwnerOnly());
        assertEquals(baseModel.isAdditionalEmailAddressesOnly(), jobDetails.isAdditionalEmailAddressesOnly());
        assertEquals(baseModel.getAttachmentFileType(), jobDetails.getAttachmentFileType());
        assertEquals(baseModel.getAdditionalEmailAddresses(), jobDetails.getAdditionalEmailAddresses());
    }

    @Test
    public void deserializeJiraCloudJobDetailsModelTest() {
        JiraCloudJobDetailsModel baseModel = new JiraCloudJobDetailsModel(true, "unknown", "JIRA-X", "bug", "done", "undone");

        DistributionJobDetailsModel deserializedModel = runDeserializerAndAssert(baseModel, DistributionJobDetailsModel::isJiraCloudDetails);

        JiraCloudJobDetailsModel jobDetails = deserializedModel.getAsJiraCouldJobDetails();
        assertEquals(baseModel.isAddComments(), jobDetails.isAddComments());
        assertEquals(baseModel.getIssueCreatorEmail(), jobDetails.getIssueCreatorEmail());
        assertEquals(baseModel.getProjectNameOrKey(), jobDetails.getProjectNameOrKey());
        assertEquals(baseModel.getIssueType(), jobDetails.getIssueType());
        assertEquals(baseModel.getResolveTransition(), jobDetails.getResolveTransition());
        assertEquals(baseModel.getReopenTransition(), jobDetails.getReopenTransition());
    }

    @Test
    public void deserializeJiraServerJobDetailsModelTest() {
        JiraServerJobDetailsModel baseModel = new JiraServerJobDetailsModel(true, "user_name01", "JIRA-Y", "other", "finished", "unfinished");

        DistributionJobDetailsModel deserializedModel = runDeserializerAndAssert(baseModel, DistributionJobDetailsModel::isJiraServerDetails);

        JiraServerJobDetailsModel jobDetails = deserializedModel.getAsJiraServerJobDetails();
        assertEquals(baseModel.isAddComments(), jobDetails.isAddComments());
        assertEquals(baseModel.getIssueCreatorUsername(), jobDetails.getIssueCreatorUsername());
        assertEquals(baseModel.getProjectNameOrKey(), jobDetails.getProjectNameOrKey());
        assertEquals(baseModel.getIssueType(), jobDetails.getIssueType());
        assertEquals(baseModel.getResolveTransition(), jobDetails.getResolveTransition());
        assertEquals(baseModel.getReopenTransition(), jobDetails.getReopenTransition());
    }

    @Test
    public void deserializeMSTeamsJobDetailsModelTest() {
        MSTeamsJobDetailsModel baseModel = new MSTeamsJobDetailsModel("webhook_url");

        DistributionJobDetailsModel deserializedModel = runDeserializerAndAssert(baseModel, DistributionJobDetailsModel::isMSTeamsDetails);

        MSTeamsJobDetailsModel jobDetails = deserializedModel.getAsMSTeamsJobDetails();
        assertEquals(baseModel.getWebhook(), jobDetails.getWebhook());
    }

    @Test
    public void deserializeSlackJobDetailsModelTest() {
        SlackJobDetailsModel baseModel = new SlackJobDetailsModel("slack_webhook_url", "a-cool-channel", "Channel Tester");

        DistributionJobDetailsModel deserializedModel = runDeserializerAndAssert(baseModel, DistributionJobDetailsModel::isSlackDetails);

        SlackJobDetailsModel jobDetails = deserializedModel.getAsSlackJobDetails();
        assertEquals(baseModel.getWebhook(), jobDetails.getWebhook());
        assertEquals(baseModel.getChannelName(), jobDetails.getChannelName());
        assertEquals(baseModel.getChannelUsername(), jobDetails.getChannelUsername());
    }

    @Test
    public void deserializeThrowsJsonParseExceptionTest() {
        DistributionJobDetailsModel baseModel = new Test_DistributionJobDetailsModel();
        JsonElement jsonElement = gson.toJsonTree(baseModel);
        DistributionJobDetailsModelJsonAdapter deserializer = new DistributionJobDetailsModelJsonAdapter();

        try {
            deserializer.deserialize(jsonElement, DistributionJobDetailsModel.class, jsonDeserializationContext);
            fail("Expected exception: " + JsonParseException.class);
        } catch (JsonParseException e) {
            // Success
        }
    }

    public void serializeAndAssert(DistributionJobDetailsModel baseModel, JsonElement baseJson) {
        DistributionJobDetailsModelJsonAdapter jsonAdapter = new DistributionJobDetailsModelJsonAdapter();
        JsonElement abstractJson = jsonAdapter.serialize(baseModel, DistributionJobDetailsModel.class, jsonSerializationContext);
        assertEquals(baseJson, abstractJson);
    }

    private DistributionJobDetailsModel runDeserializerAndAssert(DistributionJobDetailsModel baseModel, Predicate<DistributionJobDetailsModel> isSpecifiedSubclass) {
        JsonElement jsonElement = gson.toJsonTree(baseModel);
        DistributionJobDetailsModelJsonAdapter jsonAdapter = new DistributionJobDetailsModelJsonAdapter();
        DistributionJobDetailsModel deserializedModel = jsonAdapter.deserialize(jsonElement, DistributionJobDetailsModel.class, jsonDeserializationContext);
        assertTrue(isSpecifiedSubclass.test(deserializedModel), "Expected to deserialize as " + baseModel.getClass().getSimpleName());
        return deserializedModel;
    }

    private static class Test_DistributionJobDetailsModel extends DistributionJobDetailsModel {
        public Test_DistributionJobDetailsModel() {
            super("unknown_channel_name_" + RandomUtils.nextInt());
        }

    }

}
