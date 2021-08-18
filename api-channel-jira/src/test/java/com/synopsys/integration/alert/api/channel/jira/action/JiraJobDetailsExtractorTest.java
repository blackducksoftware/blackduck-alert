package com.synopsys.integration.alert.api.channel.jira.action;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraJobCustomFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.processor.DistributionJobFieldExtractor;
import com.synopsys.integration.alert.descriptor.api.model.IssueTrackerChannelKey;

public class JiraJobDetailsExtractorTest {
    private static final String CLASS_NAME = JiraJobDetailsExtractorTest.class.getSimpleName();
    private static final IssueTrackerChannelKey CHANNEL_KEY = new IssueTrackerChannelKey(CLASS_NAME, CLASS_NAME) {};
    private static final Gson GSON = new GsonBuilder().create();

    @Test
    public void extractFieldMappingsTest() {
        String testFieldName = "field.name";
        String testFieldValue = "some random value";
        JiraJobCustomFieldModel testCustomField = new JiraJobCustomFieldModel(testFieldName, testFieldValue);

        ConfigurationFieldModel testFieldModel = ConfigurationFieldModel.create(testFieldName);
        String testCustomFieldJson = GSON.toJson(testCustomField);
        testFieldModel.setFieldValue(testCustomFieldJson);

        JiraJobDetailsExtractor jobDetailsExtractor = createJobDetailsExtractor();
        List<JiraJobCustomFieldModel> fieldMappings = jobDetailsExtractor.extractJiraFieldMappings(testFieldName, Map.of(testFieldName, testFieldModel));
        assertEquals(1, fieldMappings.size());

        JiraJobCustomFieldModel extractedFieldMapping = fieldMappings.get(0);
        assertEquals(testCustomField, extractedFieldMapping);
    }

    private JiraJobDetailsExtractor createJobDetailsExtractor() {
        DistributionJobFieldExtractor fieldExtractor = new DistributionJobFieldExtractor();
        return new JiraJobDetailsExtractor(CHANNEL_KEY, fieldExtractor, GSON) {
            @Override
            public DistributionJobDetailsModel extractDetails(UUID jobId, Map<String, ConfigurationFieldModel> configuredFieldsMap) {
                return null;
            }
        };
    }

}
