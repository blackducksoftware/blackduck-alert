package com.synopsys.integration.alert.common.persistence.model.job.details;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

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
        UUID jobId = UUID.randomUUID();
        AzureBoardsJobDetailsModel baseModel = new AzureBoardsJobDetailsModel(jobId, true, "project name", "task", "none", "alt");
        JsonElement baseJson = gson.toJsonTree(baseModel);
        serializeAndAssert(baseModel, baseJson);
    }

    @Test
    public void serializeEmailJobDetailsModelTest() {
        UUID jobId = UUID.randomUUID();
        EmailJobDetailsModel baseModel = new EmailJobDetailsModel(jobId, "alert subject", false, true, null, List.of("email 1", "email 2"));
        JsonElement baseJson = gson.toJsonTree(baseModel);
        serializeAndAssert(baseModel, baseJson);
    }

    @Test
    public void serializeJiraCloudJobDetailsModelTest() {
        UUID jobId = UUID.randomUUID();
        JiraCloudJobDetailsModel baseModel = new JiraCloudJobDetailsModel(jobId, true, "unknown", "JIRA-X", "bug", "done", "undone", List.of(), null);
        JsonElement baseJson = gson.toJsonTree(baseModel);
        serializeAndAssert(baseModel, baseJson);
    }

    @Test
    public void serializeJiraServerJobDetailsModelTest() {
        UUID jobId = UUID.randomUUID();
        JiraServerJobDetailsModel baseModel = new JiraServerJobDetailsModel(jobId, true, "user_name01", "JIRA-Y", "other", "finished", "unfinished", List.of(), "issueSummary");
        JsonElement baseJson = gson.toJsonTree(baseModel);
        serializeAndAssert(baseModel, baseJson);
    }

    @Test
    public void serializeMSTeamsJobDetailsModelTest() {
        UUID jobId = UUID.randomUUID();
        MSTeamsJobDetailsModel baseModel = new MSTeamsJobDetailsModel(jobId, "webhook_url");
        JsonElement baseJson = gson.toJsonTree(baseModel);
        serializeAndAssert(baseModel, baseJson);
    }

    @Test
    public void serializeSlackJobDetailsModelTest() {
        UUID jobId = UUID.randomUUID();
        SlackJobDetailsModel baseModel = new SlackJobDetailsModel(jobId, "slack_webhook_url", "a-cool-channel", "Channel Tester");
        JsonElement baseJson = gson.toJsonTree(baseModel);
        serializeAndAssert(baseModel, baseJson);
    }

    @Test
    public void serializeAndVerifyNotEqualTest() {
        UUID jobId = UUID.randomUUID();
        MSTeamsJobDetailsModel baseModel1 = new MSTeamsJobDetailsModel(jobId, "webhook_url");
        MSTeamsJobDetailsModel baseModel2 = new MSTeamsJobDetailsModel(jobId, "different_webhook_url");

        DistributionJobDetailsModelJsonAdapter jsonAdapter = new DistributionJobDetailsModelJsonAdapter();
        JsonElement json1 = jsonAdapter.serialize(baseModel1, DistributionJobDetailsModel.class, jsonSerializationContext);
        JsonElement json2 = jsonAdapter.serialize(baseModel2, DistributionJobDetailsModel.class, jsonSerializationContext);

        assertNotEquals(json1, json2);
    }

    // DESERIALIZATION TESTS

    @Test
    public void deserializeAzureBoardsJobDetailsModelTest() {
        UUID jobId = UUID.randomUUID();
        AzureBoardsJobDetailsModel baseModel = new AzureBoardsJobDetailsModel(jobId, true, "project name", "task", "none", "alt");

        DistributionJobDetailsModel deserializedModel = runDeserializerAndAssert(baseModel, (distributionJobDetailsModel -> distributionJobDetailsModel.isA(ChannelKeys.AZURE_BOARDS)));

        AzureBoardsJobDetailsModel jobDetails = deserializedModel.getAs(DistributionJobDetailsModel.AZURE);
        assertEquals(baseModel.getJobId(), jobDetails.getJobId());
        assertEquals(baseModel.isAddComments(), jobDetails.isAddComments());
        assertEquals(baseModel.getProjectNameOrId(), jobDetails.getProjectNameOrId());
        assertEquals(baseModel.getWorkItemType(), jobDetails.getWorkItemType());
        assertEquals(baseModel.getWorkItemCompletedState(), jobDetails.getWorkItemCompletedState());
        assertEquals(baseModel.getWorkItemReopenState(), jobDetails.getWorkItemReopenState());
    }

    @Test
    public void deserializeEmailJobDetailsModelTest() {
        UUID jobId = UUID.randomUUID();
        EmailJobDetailsModel baseModel = new EmailJobDetailsModel(jobId, "alert subject", false, true, null, List.of("email 1", "email 2"));

        DistributionJobDetailsModel deserializedModel = runDeserializerAndAssert(baseModel, (distributionJobDetailsModel -> distributionJobDetailsModel.isA(ChannelKeys.EMAIL)));

        EmailJobDetailsModel jobDetails = deserializedModel.getAs(DistributionJobDetailsModel.EMAIL);
        assertEquals(baseModel.getJobId(), jobDetails.getJobId());
        assertEquals(baseModel.getSubjectLine(), jobDetails.getSubjectLine());
        assertEquals(baseModel.isProjectOwnerOnly(), jobDetails.isProjectOwnerOnly());
        assertEquals(baseModel.isAdditionalEmailAddressesOnly(), jobDetails.isAdditionalEmailAddressesOnly());
        assertEquals(baseModel.getAttachmentFileType(), jobDetails.getAttachmentFileType());
        assertEquals(baseModel.getAdditionalEmailAddresses(), jobDetails.getAdditionalEmailAddresses());
    }

    @Test
    public void deserializeJiraCloudJobDetailsModelTest() {
        UUID jobId = UUID.randomUUID();
        JiraCloudJobDetailsModel baseModel = new JiraCloudJobDetailsModel(jobId, true, "unknown", "JIRA-X", "bug", "done", "undone", List.of(), null);

        DistributionJobDetailsModel deserializedModel = runDeserializerAndAssert(baseModel, (distributionJobDetailsModel -> distributionJobDetailsModel.isA(ChannelKeys.JIRA_CLOUD)));

        JiraCloudJobDetailsModel jobDetails = deserializedModel.getAs(DistributionJobDetailsModel.JIRA_CLOUD);
        assertEquals(baseModel.getJobId(), jobDetails.getJobId());
        assertEquals(baseModel.isAddComments(), jobDetails.isAddComments());
        assertEquals(baseModel.getIssueCreatorEmail(), jobDetails.getIssueCreatorEmail());
        assertEquals(baseModel.getProjectNameOrKey(), jobDetails.getProjectNameOrKey());
        assertEquals(baseModel.getIssueType(), jobDetails.getIssueType());
        assertEquals(baseModel.getResolveTransition(), jobDetails.getResolveTransition());
        assertEquals(baseModel.getReopenTransition(), jobDetails.getReopenTransition());
    }

    @Test
    public void deserializeJiraServerJobDetailsModelTest() {
        UUID jobId = UUID.randomUUID();
        JiraServerJobDetailsModel baseModel = new JiraServerJobDetailsModel(jobId, true, "user_name01", "JIRA-Y", "other", "finished", "unfinished", List.of(), "issueSummary");

        DistributionJobDetailsModel deserializedModel = runDeserializerAndAssert(baseModel, (distributionJobDetailsModel -> distributionJobDetailsModel.isA(ChannelKeys.JIRA_SERVER)));

        JiraServerJobDetailsModel jobDetails = deserializedModel.getAs(DistributionJobDetailsModel.JIRA_SERVER);
        assertEquals(baseModel.getJobId(), jobDetails.getJobId());
        assertEquals(baseModel.isAddComments(), jobDetails.isAddComments());
        assertEquals(baseModel.getIssueCreatorUsername(), jobDetails.getIssueCreatorUsername());
        assertEquals(baseModel.getProjectNameOrKey(), jobDetails.getProjectNameOrKey());
        assertEquals(baseModel.getIssueType(), jobDetails.getIssueType());
        assertEquals(baseModel.getResolveTransition(), jobDetails.getResolveTransition());
        assertEquals(baseModel.getReopenTransition(), jobDetails.getReopenTransition());
    }

    @Test
    public void deserializeMSTeamsJobDetailsModelTest() {
        UUID jobId = UUID.randomUUID();
        MSTeamsJobDetailsModel baseModel = new MSTeamsJobDetailsModel(jobId, "webhook_url");

        DistributionJobDetailsModel deserializedModel = runDeserializerAndAssert(baseModel, (distributionJobDetailsModel -> distributionJobDetailsModel.isA(ChannelKeys.MS_TEAMS)));

        MSTeamsJobDetailsModel jobDetails = deserializedModel.getAs(DistributionJobDetailsModel.MS_TEAMS);
        assertEquals(baseModel.getJobId(), jobDetails.getJobId());
        assertEquals(baseModel.getWebhook(), jobDetails.getWebhook());
    }

    @Test
    public void deserializeSlackJobDetailsModelTest() {
        UUID jobId = UUID.randomUUID();
        SlackJobDetailsModel baseModel = new SlackJobDetailsModel(jobId, "slack_webhook_url", "a-cool-channel", "Channel Tester");

        DistributionJobDetailsModel deserializedModel = runDeserializerAndAssert(baseModel, (distributionJobDetailsModel -> distributionJobDetailsModel.isA(ChannelKeys.SLACK)));

        SlackJobDetailsModel jobDetails = deserializedModel.getAs(DistributionJobDetailsModel.SLACK);
        assertEquals(baseModel.getJobId(), jobDetails.getJobId());
        assertEquals(baseModel.getWebhook(), jobDetails.getWebhook());
        assertEquals(baseModel.getChannelName(), jobDetails.getChannelName());
        assertEquals(baseModel.getChannelUsername(), jobDetails.getChannelUsername());
    }

    @Test
    public void deserializeThrowsJsonParseExceptionTest() {
        String testFieldValue = "a value";
        DistributionJobDetailsModel baseModel = new Test_DistributionJobDetailsModel(testFieldValue);
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
        private static final ChannelKey TEST_CHANNEL_KEY = new ChannelKey("unknown_channel_name_" + RandomUtils.nextInt(), "unknown");
        private final String testField;

        public Test_DistributionJobDetailsModel(String testField) {
            super(TEST_CHANNEL_KEY, UUID.randomUUID());
            this.testField = testField;
        }

        public String getTestField() {
            return testField;
        }

    }

}
