package com.synopsys.integration.alert.workflow.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import com.synopsys.integration.alert.common.configuration.CommonDistributionConfiguration;
import com.synopsys.integration.alert.common.descriptor.config.ui.CommonDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.field.JsonField;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor.ConfigurationModel;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationFieldModel;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDistributionUIConfig;
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

        final JsonField<String> stringField = JsonField.createStringField(JsonPath.compile("$.innerString"), "innerString", null, null);
        final JsonField<DummyClass> objectField = JsonField.createObjectField(JsonPath.compile("$.innerObject"), "innerObject", null, null, new TypeRef<List<DummyClass>>() {});
        final List<JsonField<?>> fields = Arrays.asList(stringField, objectField);

        final JsonFieldAccessor accessor = jsonExtractor.createJsonFieldAccessor(fields, json);
        Assert.assertEquals(innerString, accessor.get(stringField).get(0));
        Assert.assertEquals(innerObject, accessor.get(objectField).get(0));
    }

    @Test
    public void getValuesFromJsonTest() {
        final String key = "innerField";
        final String value = "thing that I want";
        final String json = "{\"content\":{\"someObject\":{\"" + key + "\":\"" + value + "\"}}}";
        final JsonField<String> field = JsonField.createStringField(JsonPath.compile("$.content.someObject.innerField"), "innerField", null, null);
        final List<String> values = jsonExtractor.getValuesFromJson(field, json);
        Assert.assertEquals(Arrays.asList(value), values);
    }

    @Test
    public void getValuesFromJsonWithArrayTest() {
        final String key = "innerField";
        final String value1 = "thing that I want";
        final String value2 = "other thing that I want";
        final String json = "{\"content\":{\"someObject\":[{\"" + key + "\":\"" + value1 + "\"},{\"" + key + "\":\"" + value2 + "\"}]}}";
        final JsonField<String> field = JsonField.createStringField(JsonPath.compile("$.content.someObject[*].innerField"), "innerField", null, null);
        final List<String> values = jsonExtractor.getValuesFromJson(field, json);
        Assert.assertEquals(Arrays.asList(value1, value2), values);
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
                createConfigModel(id, distributionConfigId, distributionType, name, providerName, frequency, filterByProject, projectNamePattern, configuredProjects,
                        notificationTypes, FormatType.DEFAULT.name()));

        final JsonField<String> nameField = JsonField.createStringField(null, null, null, null, Arrays.asList(JsonPath.compile("$.name")));
        final List<String> nameValues = jsonExtractor.getValuesFromConfig(nameField, commonDistributionConfig);
        Assert.assertEquals(Arrays.asList(name), nameValues);

        final JsonField<String> configuredProjectsField = JsonField.createStringField(null, null, null, null, Arrays.asList(JsonPath.compile("$.configuredProjects[*]")));
        final List<String> configuredProjectValues = jsonExtractor.getValuesFromConfig(configuredProjectsField, commonDistributionConfig);
        Assert.assertEquals(configuredProjects, configuredProjectValues);

        final JsonField<String> notificationTypesField = JsonField.createStringField(null, null, null, null, Arrays.asList(JsonPath.compile("$.notificationTypes[*]")));
        final List<String> notificationTypeValues = jsonExtractor.getValuesFromConfig(notificationTypesField, commonDistributionConfig);
        Assert.assertEquals(notificationTypes, notificationTypeValues);
    }

    private ConfigurationModel createConfigModel(final Long id, final Long descriptorId, final String distributionType, final String name, final String providerName, final String frequency, final String filterByProject,
            final String projectNamePattern, final List<String> configuredProjects, final List<String> notificationTypes, final String formatType) {
        final ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);

        Mockito.when(configurationModel.getConfigurationId()).thenReturn(id);
        Mockito.when(configurationModel.getDescriptorId()).thenReturn(descriptorId);

        final List<ConfigurationFieldModel> fieldList = new ArrayList<>();
        mockField(fieldList, configurationModel, CommonDistributionUIConfig.KEY_NAME, name);
        mockField(fieldList, configurationModel, CommonDistributionUIConfig.KEY_FREQUENCY, frequency);
        mockField(fieldList, configurationModel, CommonDistributionUIConfig.KEY_PROVIDER_NAME, providerName);
        // FIXME mockField(fieldList, configurationModel, CommonDistributionUIConfig.KEY_CHANNEL_NAME, channelName);
        // FIXME do we need to add distributionType?

        mockField(fieldList, configurationModel, ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES, notificationTypes);
        mockField(fieldList, configurationModel, ProviderDistributionUIConfig.KEY_FORMAT_TYPE, formatType);

        mockField(fieldList, configurationModel, BlackDuckDistributionUIConfig.KEY_FILTER_BY_PROJECT, filterByProject);
        mockField(fieldList, configurationModel, BlackDuckDistributionUIConfig.KEY_PROJECT_NAME_PATTERN, projectNamePattern);
        mockField(fieldList, configurationModel, BlackDuckDistributionUIConfig.KEY_CONFIGURED_PROJECT, configuredProjects);

        return configurationModel;
    }

    private void mockField(final List<ConfigurationFieldModel> fieldList, final ConfigurationModel configurationModel, final String key, final String value) {
        mockField(fieldList, configurationModel, key, List.of(value));
    }

    private void mockField(final List<ConfigurationFieldModel> fieldList, final ConfigurationModel configurationModel, final String key, final Collection<String> values) {
        final ConfigurationFieldModel field = ConfigurationFieldModel.create(key);
        field.setFieldValues(values);
        Mockito.when(configurationModel.getField(key)).thenReturn(Optional.of(field));
        fieldList.add(field);
    }

    private class DummyClass extends Object {
        public String dummyVariable;

        @Override
        public boolean equals(final Object obj) {
            return dummyVariable != null && DummyClass.class.isAssignableFrom(obj.getClass()) && dummyVariable.equals(((DummyClass) obj).dummyVariable);
        }
    }
}
