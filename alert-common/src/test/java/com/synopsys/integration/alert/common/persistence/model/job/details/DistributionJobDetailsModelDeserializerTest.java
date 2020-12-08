package com.synopsys.integration.alert.common.persistence.model.job.details;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class DistributionJobDetailsModelDeserializerTest {
    private final Gson gson = new Gson();
    private final JsonDeserializationContext jsonDeserializationContext = gson::fromJson;

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
        DistributionJobDetailsModelDeserializer deserializer = new DistributionJobDetailsModelDeserializer();

        try {
            deserializer.deserialize(jsonElement, DistributionJobDetailsModel.class, jsonDeserializationContext);
            fail("Expected exception: " + JsonParseException.class);
        } catch (JsonParseException e) {
            // Success
        }
    }

    private DistributionJobDetailsModel runDeserializerAndAssert(DistributionJobDetailsModel baseModel, Predicate<DistributionJobDetailsModel> isSpecifiedSubclass) {
        JsonElement jsonElement = gson.toJsonTree(baseModel);
        DistributionJobDetailsModelDeserializer deserializer = new DistributionJobDetailsModelDeserializer();
        DistributionJobDetailsModel deserializedModel = deserializer.deserialize(jsonElement, DistributionJobDetailsModel.class, jsonDeserializationContext);
        assertTrue(isSpecifiedSubclass.test(deserializedModel), "Expected to deserialize as " + baseModel.getClass().getSimpleName());
        return deserializedModel;
    }

    private static class Test_DistributionJobDetailsModel extends DistributionJobDetailsModel {
        public Test_DistributionJobDetailsModel() {
            super("unknown_channel_name_" + RandomUtils.nextInt());
        }

    }

}
