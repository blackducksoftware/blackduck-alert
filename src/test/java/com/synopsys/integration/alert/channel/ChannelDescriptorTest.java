package com.synopsys.integration.alert.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.slack.SlackChannelKey;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertFieldStatus;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.util.DataStructureUtils;
import com.synopsys.integration.alert.database.api.DefaultConfigurationAccessor;
import com.synopsys.integration.alert.database.api.DefaultDescriptorAccessor;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.TestProperties;
import com.synopsys.integration.alert.web.config.FieldValidationAction;
import com.synopsys.integration.exception.IntegrationException;

@Transactional
public abstract class ChannelDescriptorTest extends AlertIntegrationTest {
    protected Gson gson;
    protected DistributionEvent channelEvent;

    protected ContentConverter contentConverter;
    protected TestProperties properties;

    @Autowired
    protected DefaultConfigurationAccessor configurationAccessor;
    @Autowired
    protected DefaultDescriptorAccessor descriptorAccessor;
    @Autowired
    protected RegisteredDescriptorRepository registeredDescriptorRepository;

    protected ConfigurationModel provider_global;
    protected Optional<ConfigurationModel> global_config;
    protected ConfigurationModel distribution_config;
    protected String destinationName;

    @BeforeEach
    public void init() throws Exception {
        gson = new Gson();
        contentConverter = new ContentConverter(gson, new DefaultConversionService());
        properties = new TestProperties();
        global_config = saveGlobalConfiguration();
        distribution_config = saveDistributionConfiguration();
        channelEvent = createChannelEvent();
        destinationName = getDestinationName();
    }

    @AfterEach
    public void cleanupTest() throws Exception {
        if (null != global_config && global_config.isPresent()) {
            configurationAccessor.deleteConfiguration(global_config.get());
        }

        if (distribution_config != null) {
            configurationAccessor.deleteConfiguration(distribution_config);
        }

        if (provider_global != null) {
            configurationAccessor.deleteConfiguration(provider_global);
        }
    }

    public Map<String, FieldValueModel> createFieldModelMap(List<ConfigurationFieldModel> configFieldModels) {
        Map<String, FieldValueModel> fieldModelMap = new HashMap<>();
        for (ConfigurationFieldModel model : configFieldModels) {
            String key = model.getFieldKey();
            Collection<String> values = List.of();
            if (!model.isSensitive()) {
                values = model.getFieldValues();
            }
            FieldValueModel fieldValueModel = new FieldValueModel(values, model.isSet());
            fieldModelMap.put(key, fieldValueModel);
        }
        return fieldModelMap;
    }

    public FieldModel createValidFieldModel(ConfigurationModel configurationModel, ConfigContextEnum context) {
        Map<String, FieldValueModel> fieldValueMap = createFieldModelMap(configurationModel.getCopyOfFieldList());
        if (ConfigContextEnum.DISTRIBUTION == context) {
            global_config.ifPresent(globalConfig -> fieldValueMap.putAll(createFieldModelMap(globalConfig.getCopyOfFieldList())));
        }
        FieldModel model = new FieldModel(String.valueOf(configurationModel.getConfigurationId()), destinationName, context.name(), fieldValueMap);
        return model;
    }

    public FieldAccessor createValidFieldAccessor(ConfigurationModel configurationModel) {
        Map<String, ConfigurationFieldModel> fieldMap = new HashMap<>();
        fieldMap.putAll(configurationModel.getCopyOfKeyToFieldMap());
        global_config.ifPresent(globalConfig -> fieldMap.putAll(globalConfig.getCopyOfKeyToFieldMap()));
        FieldAccessor fieldAccessor = new FieldAccessor(fieldMap);
        return fieldAccessor;

    }

    public FieldModel createInvalidDistributionFieldModel() {
        Map<String, String> invalidValuesMap = new HashMap<>();
        invalidValuesMap.putAll(createInvalidCommonDistributionFieldMap());
        invalidValuesMap.putAll(createInvalidDistributionFieldMap());

        Map<String, FieldValueModel> fieldModelMap = createFieldValueModelMap(invalidValuesMap);
        FieldModel model = new FieldModel("1L", destinationName, ConfigContextEnum.DISTRIBUTION.name(), fieldModelMap);
        return model;
    }

    public Map<String, String> createValidCommonDistributionFieldMap() {
        BlackDuckProviderKey blackDuckProviderKey = new BlackDuckProviderKey();
        SlackChannelKey slackChannelKey = new SlackChannelKey();
        return Map.of(ChannelDistributionUIConfig.KEY_NAME, "name", ChannelDistributionUIConfig.KEY_FREQUENCY, FrequencyType.REAL_TIME.name(), ChannelDistributionUIConfig.KEY_CHANNEL_NAME, slackChannelKey.getUniversalKey(),
            ChannelDistributionUIConfig.KEY_PROVIDER_NAME, blackDuckProviderKey.getUniversalKey());
    }

    public Map<String, String> createInvalidCommonDistributionFieldMap() {
        Map<String, String> invalidValuesMap = Map.of(ChannelDistributionUIConfig.KEY_NAME, "", ChannelDistributionUIConfig.KEY_FREQUENCY, "", ChannelDistributionUIConfig.KEY_CHANNEL_NAME, "",
            ChannelDistributionUIConfig.KEY_PROVIDER_NAME, "");
        return invalidValuesMap;
    }

    public FieldModel createInvalidGlobalFieldModel() {
        Map<String, String> invalidValuesMap = createInvalidGlobalFieldMap();
        Map<String, FieldValueModel> fieldModelMap = createFieldValueModelMap(invalidValuesMap);
        FieldModel model = new FieldModel("1L", destinationName, ConfigContextEnum.GLOBAL.name(), fieldModelMap);
        return model;
    }

    public Map<String, FieldValueModel> createFieldValueModelMap(Map<String, String> fieldValueMap) {
        Map<String, FieldValueModel> fieldModelMap = new HashMap<>();
        for (Map.Entry<String, String> fieldValue : fieldValueMap.entrySet()) {
            String key = fieldValue.getKey();
            String value = fieldValue.getValue();
            FieldValueModel fieldValueModel = new FieldValueModel(List.of(value), StringUtils.isNotBlank(value));
            fieldModelMap.put(key, fieldValueModel);
        }
        return fieldModelMap;
    }

    public FieldModel createFieldModel(String descriptorName, String emailAddress) {
        String context = ConfigContextEnum.DISTRIBUTION.name();
        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        keyToValues.put(TestAction.KEY_DESTINATION_NAME, new FieldValueModel(List.of(emailAddress), false));
        return new FieldModel(descriptorName, context, keyToValues);
    }

    public abstract DistributionEvent createChannelEvent() throws AlertException;

    public abstract Optional<ConfigurationModel> saveGlobalConfiguration() throws Exception;

    public abstract ConfigurationModel saveDistributionConfiguration() throws Exception;

    public abstract ChannelDescriptor getDescriptor();

    public abstract boolean assertGlobalFields(Set<DefinedFieldModel> globalFields);

    public abstract boolean assertDistributionFields(Set<DefinedFieldModel> distributionFields);

    public abstract FieldModel createTestConfigDestination();

    public abstract Map<String, String> createInvalidGlobalFieldMap();

    public abstract Map<String, String> createInvalidDistributionFieldMap();

    public abstract String getTestJobName();

    public abstract String getDestinationName();

    public abstract TestAction getTestAction();

    private Map<String, ConfigField> createFieldMap(ConfigContextEnum context) {
        return getDescriptor().getUIConfig(context)
                   .map(uiConfig -> DataStructureUtils.mapToValues(uiConfig.getFields(), ConfigField::getKey))
                   .orElse(Map.of());
    }

    @Test
    public void testDistributionConfig() {
        FieldAccessor fieldAccessor = createValidFieldAccessor(distribution_config);

        FieldModel fieldModel = createTestConfigDestination();
        try {
            TestAction descriptorActionApi = getTestAction();
            descriptorActionApi.testConfig(String.valueOf(distribution_config.getConfigurationId()), fieldModel, fieldAccessor);
        } catch (IntegrationException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGlobalConfig() {
        ConfigurationModel configurationModel = global_config.orElse(null);
        FieldAccessor fieldAccessor = createValidFieldAccessor(configurationModel);
        try {
            TestAction descriptorActionApi = getTestAction();
            descriptorActionApi.testConfig(String.valueOf(configurationModel.getConfigurationId()), createTestConfigDestination(), fieldAccessor);
        } catch (IntegrationException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCreateChannelEvent() throws Exception {
        DistributionEvent channelEvent = createChannelEvent();
        assertEquals(String.valueOf(distribution_config.getConfigurationId()), channelEvent.getConfigId());
        assertEquals(36, channelEvent.getEventId().length());
        assertEquals(destinationName, channelEvent.getDestination());
    }

    @Test
    public void testDistributionValidate() {
        FieldModel restModel = createValidFieldModel(distribution_config, ConfigContextEnum.DISTRIBUTION);
        FieldValueModel jobNameField = restModel.getFieldValueModel(ChannelDistributionUIConfig.KEY_NAME).orElseThrow();
        jobNameField.setValue(getTestJobName());
        HashMap<String, AlertFieldStatus> fieldErrors = new HashMap<>();
        Map<String, ConfigField> configFieldMap = createFieldMap(ConfigContextEnum.DISTRIBUTION);
        FieldValidationAction fieldValidationAction = new FieldValidationAction();
        fieldValidationAction.validateConfig(configFieldMap, restModel, fieldErrors);
        assertTrue(fieldErrors.isEmpty());
    }

    @Test
    public void testDistributionValidateWithFieldErrors() {
        FieldModel restModel = createInvalidDistributionFieldModel();
        HashMap<String, AlertFieldStatus> fieldErrors = new HashMap<>();
        Map<String, ConfigField> configFieldMap = createFieldMap(ConfigContextEnum.DISTRIBUTION);
        FieldValidationAction fieldValidationAction = new FieldValidationAction();
        fieldValidationAction.validateConfig(configFieldMap, restModel, fieldErrors);

        if (restModel.getKeyToValues().size() > 0) {
            assertEquals(restModel.getKeyToValues().size(), fieldErrors.size());
        }
    }

    @Test
    public void testGlobalValidate() {
        FieldModel restModel = createValidFieldModel(global_config.orElse(null), ConfigContextEnum.GLOBAL);
        HashMap<String, AlertFieldStatus> fieldErrors = new HashMap<>();
        Map<String, ConfigField> configFieldMap = createFieldMap(ConfigContextEnum.GLOBAL);
        FieldValidationAction fieldValidationAction = new FieldValidationAction();
        fieldValidationAction.validateConfig(configFieldMap, restModel, fieldErrors);
        List<String> errors = fieldErrors.entrySet()
                                  .stream()
                                  .map(entry -> entry.getKey() + " : " + entry.getValue()
                                  ).collect(Collectors.toList());
        assertTrue(fieldErrors.isEmpty(), StringUtils.join(errors, "; "));
    }

    @Test
    public void testGlobalValidateWithFieldErrors() {
        // descriptor has a global configuration therefore continue testing
        FieldModel restModel = createInvalidGlobalFieldModel();
        HashMap<String, AlertFieldStatus> fieldErrors = new HashMap<>();
        Map<String, ConfigField> configFieldMap = createFieldMap(ConfigContextEnum.GLOBAL);
        FieldValidationAction fieldValidationAction = new FieldValidationAction();
        fieldValidationAction.validateConfig(configFieldMap, restModel, fieldErrors);
        assertEquals(restModel.getKeyToValues().size(), fieldErrors.size());
    }

    @Test
    public void testDefinedFields() {
        assertTrue(assertGlobalFields(getDescriptor().getAllDefinedFields(ConfigContextEnum.GLOBAL)));
        assertTrue(assertDistributionFields(getDescriptor().getAllDefinedFields(ConfigContextEnum.DISTRIBUTION)));
        assertTrue(getDescriptor().getAllDefinedFields(null).isEmpty());
    }

}
