package com.synopsys.integration.alert.workflow.filter;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.field.StringHierarchicalField;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;

public class JsonExtractorTest {

    public JsonExtractor jsonExtractor;

    @Before
    public void init() {
        jsonExtractor = new JsonExtractor(new Gson());
    }

    // TODO add more test cases for new methods

    @Test
    public void getValuesFromJsonTest() {
        final String key = "innerField";
        final String value = "thing that I want";
        final String json = "{\"content\":{\"someObject\":{\"" + key + "\":\"" + value + "\"}}}";
        final List<String> pathToField = Arrays.asList("content", "someObject");
        final StringHierarchicalField field = new StringHierarchicalField(pathToField, key, null, null);
        final List<String> values = jsonExtractor.getValuesFromJson(field, json);
        Assert.assertEquals(Arrays.asList(value), values);
    }

    @Test
    public void getValuesFromJsonWithArrayTest() {
        final String key = "innerField";
        final String value1 = "thing that I want";
        final String value2 = "other thing that I want";
        final String json = "{\"content\":{\"someObject\":[{\"" + key + "\":\"" + value1 + "\"},{\"" + key + "\":\"" + value2 + "\"}]}}";
        final List<String> pathToField = Arrays.asList("content", "someObject");
        final StringHierarchicalField field = new StringHierarchicalField(pathToField, key, null, null);
        final List<String> values = jsonExtractor.getValuesFromJson(field, json);
        Assert.assertEquals(Arrays.asList(value1, value2), values);
    }

    @Test
    public void getValuesFromConfig() {
        final String id = "1";
        final String distributionConfigId = "1";
        final String distributionType = "generic";
        final String name = "example";
        final String providerName = "some provider";
        final String frequency = "REAL_TIME";
        final String filterByProject = "true";
        final List<String> configuredProjects = Arrays.asList("project1", "project2", "project3");
        final List<String> notificationTypes = Arrays.asList("type1", "type2");
        final CommonDistributionConfig commonDistributionConfig = new CommonDistributionConfig(id, distributionConfigId, distributionType, name, providerName, frequency, filterByProject, configuredProjects, notificationTypes,
            FormatType.DEFAULT.name());

        final StringHierarchicalField nameField = new StringHierarchicalField(Arrays.asList(), null, null, null, "name");
        final List<String> nameValues = jsonExtractor.getValuesFromConfig(nameField, commonDistributionConfig);
        Assert.assertEquals(Arrays.asList(name), nameValues);

        final StringHierarchicalField configuredProjectsField = new StringHierarchicalField(Arrays.asList(), null, null, null, "configuredProjects");
        final List<String> configuredProjectValues = jsonExtractor.getValuesFromConfig(configuredProjectsField, commonDistributionConfig);
        Assert.assertEquals(configuredProjects, configuredProjectValues);

        final StringHierarchicalField notificationTypesField = new StringHierarchicalField(Arrays.asList(), null, null, null, "notificationTypes");
        final List<String> notificationTypeValues = jsonExtractor.getValuesFromConfig(notificationTypesField, commonDistributionConfig);
        Assert.assertEquals(notificationTypes, notificationTypeValues);
    }
}
