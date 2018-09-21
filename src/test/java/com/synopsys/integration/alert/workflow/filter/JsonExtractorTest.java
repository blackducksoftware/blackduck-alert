package com.synopsys.integration.alert.workflow.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.field.HierarchicalField;
import com.synopsys.integration.alert.common.field.ObjectHierarchicalField;
import com.synopsys.integration.alert.common.field.StringHierarchicalField;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;
import com.synopsys.integration.alert.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.workflow.filter.field.JsonFieldAccessor;

public class JsonExtractorTest {
    private final Gson gson = new Gson();
    private final JsonExtractor jsonExtractor = new JsonExtractor(gson);

    @Test
    public void createJsonFieldAccessorTest() {
        final String innerString = "example string";
        final List<String> stringValues = new ArrayList<>();
        stringValues.add(innerString);

        final DummyClass innerObject = new DummyClass();
        innerObject.dummyVariable = "example";
        final List<Object> objectValues = new ArrayList<>();
        objectValues.add(innerObject);

        final String innerObjectJson = gson.toJson(innerObject);
        final String json = "{\"innerString\":\"" + innerString + "\",\"innerObject\":" + innerObjectJson + "}";

        final StringHierarchicalField stringField = new StringHierarchicalField(Collections.emptyList(), "innerString", null, null);
        final ObjectHierarchicalField objectField = new ObjectHierarchicalField(Collections.emptyList(), "innerObject", null, null, new TypeToken<DummyClass>() {}.getType());
        final List<HierarchicalField> fields = Arrays.asList(stringField, objectField);

        final JsonFieldAccessor accessor = jsonExtractor.createJsonFieldAccessor(fields, json);
        Assert.assertEquals(innerString, accessor.get(stringField).get(0));
        Assert.assertEquals(innerObject, accessor.get(objectField).get(0));
    }

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

    private class DummyClass extends Object {
        public String dummyVariable;

        @Override
        public boolean equals(final Object obj) {
            return dummyVariable != null && DummyClass.class.isAssignableFrom(obj.getClass()) && dummyVariable.equals(((DummyClass) obj).dummyVariable);
        }
    }
}
