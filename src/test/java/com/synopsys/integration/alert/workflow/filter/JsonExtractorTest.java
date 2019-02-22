package com.synopsys.integration.alert.workflow.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.rest.model.CommonDistributionConfiguration;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonField;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonFieldAccessor;
import com.synopsys.integration.alert.mock.MockConfigurationModelFactory;

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

        final JsonField<String> stringField = JsonField.createStringField(JsonPath.compile("$.innerString"), "innerString", null, null);
        final JsonField<DummyClass> objectField = JsonField.createObjectField(JsonPath.compile("$.innerObject"), "innerObject", null, null, new TypeRef<List<DummyClass>>() {});
        final List<JsonField<?>> fields = Arrays.asList(stringField, objectField);

        final JsonFieldAccessor accessor = jsonExtractor.createJsonFieldAccessor(fields, json);
        assertEquals(innerString, accessor.get(stringField).get(0));
        assertEquals(innerObject, accessor.get(objectField).get(0));
    }

    @Test
    public void getValuesFromJsonTest() {
        final String key = "innerField";
        final String value = "thing that I want";
        final String json = "{\"content\":{\"someObject\":{\"" + key + "\":\"" + value + "\"}}}";
        final JsonField<String> field = JsonField.createStringField(JsonPath.compile("$.content.someObject.innerField"), "innerField", null, null);
        final List<String> values = jsonExtractor.getValuesFromJson(field, json);
        assertEquals(Arrays.asList(value), values);
    }

    @Test
    public void getValuesFromJsonWithArrayTest() {
        final String key = "innerField";
        final String value1 = "thing that I want";
        final String value2 = "other thing that I want";
        final String json = "{\"content\":{\"someObject\":[{\"" + key + "\":\"" + value1 + "\"},{\"" + key + "\":\"" + value2 + "\"}]}}";
        final JsonField<String> field = JsonField.createStringField(JsonPath.compile("$.content.someObject[*].innerField"), "innerField", null, null);
        final List<String> values = jsonExtractor.getValuesFromJson(field, json);
        assertEquals(Arrays.asList(value1, value2), values);
    }

    @Test
    public void getValuesFromConfig() {
        final Long id = 1L;
        final Long distributionConfigId = 1L;
        final String distributionType = "generic";
        final String name = "example";
        final String providerName = "some provider";
        final String frequency = "REAL_TIME";
        final String filterByProject = "true";
        final String projectNamePattern = "projectNamePattern";
        final List<String> configuredProjects = Arrays.asList("project1", "project2", "project3");
        final List<String> notificationTypes = Arrays.asList("type1", "type2");

        final CommonDistributionConfiguration commonDistributionConfig = new CommonDistributionConfiguration(
            MockConfigurationModelFactory.createCommonJobConfigModel(UUID.randomUUID(), id, distributionConfigId, distributionType, name, providerName, frequency, filterByProject, projectNamePattern, configuredProjects,
                notificationTypes, FormatType.DEFAULT.name()));

        final JsonField<String> nameField = JsonField.createStringField(null, null, null, null, Arrays.asList(JsonPath.compile("$.name")));
        final List<String> nameValues = jsonExtractor.getValuesFromConfig(nameField, commonDistributionConfig);
        assertEquals(Arrays.asList(name), nameValues);

        final JsonField<String> configuredProjectsField = JsonField.createStringField(null, null, null, null, Arrays.asList(JsonPath.compile("$.configuredProjects[*]")));
        final List<String> configuredProjectValues = jsonExtractor.getValuesFromConfig(configuredProjectsField, commonDistributionConfig);
        assertEquals(configuredProjects.size(), configuredProjectValues.size());

        final JsonField<String> notificationTypesField = JsonField.createStringField(null, null, null, null, Arrays.asList(JsonPath.compile("$.notificationTypes[*]")));
        final List<String> notificationTypeValues = jsonExtractor.getValuesFromConfig(notificationTypesField, commonDistributionConfig);
        assertEquals(notificationTypes.size(), notificationTypeValues.size());
    }

    private class DummyClass extends Object {
        public String dummyVariable;

        @Override
        public boolean equals(final Object obj) {
            return dummyVariable != null && DummyClass.class.isAssignableFrom(obj.getClass()) && dummyVariable.equals(((DummyClass) obj).dummyVariable);
        }
    }
}
