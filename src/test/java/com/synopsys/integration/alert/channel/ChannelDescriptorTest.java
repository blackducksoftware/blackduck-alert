package com.synopsys.integration.alert.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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
import com.synopsys.integration.alert.channel.hipchat.HipChatChannel;
import com.synopsys.integration.alert.channel.slack.SlackChannel;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.TestConfigModel;
import com.synopsys.integration.alert.database.api.DefaultConfigurationAccessor;
import com.synopsys.integration.alert.database.api.DefaultDescriptorAccessor;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
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

    public Map<String, FieldValueModel> createFieldModelMap(final List<ConfigurationFieldModel> configFieldModels) {
        final Map<String, FieldValueModel> fieldModelMap = new HashMap<>();
        for (final ConfigurationFieldModel model : configFieldModels) {
            final String key = model.getFieldKey();
            Collection<String> values = List.of();
            if (!model.isSensitive()) {
                values = model.getFieldValues();
            }
            final FieldValueModel fieldValueModel = new FieldValueModel(values, model.isSet());
            fieldModelMap.put(key, fieldValueModel);
        }
        return fieldModelMap;
    }

    public FieldModel createValidFieldModel(final ConfigurationModel configurationModel, final ConfigContextEnum context) {
        final Map<String, FieldValueModel> fieldValueMap = createFieldModelMap(configurationModel.getCopyOfFieldList());
        if (ConfigContextEnum.DISTRIBUTION == context) {
            global_config.ifPresent(globalConfig -> fieldValueMap.putAll(createFieldModelMap(globalConfig.getCopyOfFieldList())));
        }
        final FieldModel model = new FieldModel(String.valueOf(configurationModel.getConfigurationId()), destinationName, context.name(), fieldValueMap);
        return model;
    }

    public FieldAccessor createValidFieldAccessor(final ConfigurationModel configurationModel) {
        final Map<String, ConfigurationFieldModel> fieldMap = new HashMap<>();
        fieldMap.putAll(configurationModel.getCopyOfKeyToFieldMap());
        global_config.ifPresent(globalConfig -> fieldMap.putAll(globalConfig.getCopyOfKeyToFieldMap()));
        final FieldAccessor fieldAccessor = new FieldAccessor(fieldMap);
        return fieldAccessor;

    }

    public FieldModel createInvalidDistributionFieldModel() {
        final Map<String, String> invalidValuesMap = new HashMap<>();
        invalidValuesMap.putAll(createInvalidCommonDistributionFieldMap());
        invalidValuesMap.putAll(createInvalidDistributionFieldMap());

        final Map<String, FieldValueModel> fieldModelMap = createFieldValueModelMap(invalidValuesMap);
        final FieldModel model = new FieldModel("1L", destinationName, ConfigContextEnum.DISTRIBUTION.name(), fieldModelMap);
        return model;
    }

    public Map<String, String> createValidCommonDistributionFieldMap() {
        return Map.of(ChannelDistributionUIConfig.KEY_NAME, "name", ChannelDistributionUIConfig.KEY_FREQUENCY, FrequencyType.REAL_TIME.name(), ChannelDistributionUIConfig.KEY_CHANNEL_NAME, SlackChannel.COMPONENT_NAME,
            ChannelDistributionUIConfig.KEY_PROVIDER_NAME, BlackDuckProvider.COMPONENT_NAME);
    }

    public Map<String, String> createInvalidCommonDistributionFieldMap() {
        final Map<String, String> invalidValuesMap = Map.of(ChannelDistributionUIConfig.KEY_NAME, "", ChannelDistributionUIConfig.KEY_FREQUENCY, "", ChannelDistributionUIConfig.KEY_CHANNEL_NAME, "",
            ChannelDistributionUIConfig.KEY_PROVIDER_NAME, "");
        return invalidValuesMap;
    }

    public FieldModel createInvalidGlobalFieldModel() {
        final Map<String, String> invalidValuesMap = createInvalidGlobalFieldMap();
        final Map<String, FieldValueModel> fieldModelMap = createFieldValueModelMap(invalidValuesMap);
        final FieldModel model = new FieldModel("1L", destinationName, ConfigContextEnum.GLOBAL.name(), fieldModelMap);
        return model;
    }

    public Map<String, FieldValueModel> createFieldValueModelMap(final Map<String, String> fieldValueMap) {
        final Map<String, FieldValueModel> fieldModelMap = new HashMap<>();
        for (final Map.Entry<String, String> fieldValue : fieldValueMap.entrySet()) {
            final String key = fieldValue.getKey();
            final String value = fieldValue.getValue();
            final FieldValueModel fieldValueModel = new FieldValueModel(List.of(value), StringUtils.isNotBlank(value));
            fieldModelMap.put(key, fieldValueModel);
        }
        return fieldModelMap;
    }

    public abstract DistributionEvent createChannelEvent();

    public abstract Optional<ConfigurationModel> saveGlobalConfiguration() throws Exception;

    public abstract ConfigurationModel saveDistributionConfiguration() throws Exception;

    public abstract ChannelDescriptor getDescriptor();

    public abstract boolean assertGlobalFields(Set<DefinedFieldModel> globalFields);

    public abstract boolean assertDistributionFields(Set<DefinedFieldModel> distributionFields);

    public abstract String createTestConfigDestination();

    public abstract Map<String, String> createInvalidGlobalFieldMap();

    public abstract Map<String, String> createInvalidDistributionFieldMap();

    public abstract String getTestJobName();

    public abstract String getDestinationName();

    public abstract TestAction getTestAction();

    private Map<String, ConfigField> createFieldMap(final ConfigContextEnum context) {
        return getDescriptor().getUIConfig(context)
                   .flatMap(uiConfig -> Optional.of(uiConfig.createFields()
                                                        .stream()
                                                        .collect(Collectors.toMap(ConfigField::getKey, Function.identity()))))
                   .orElse(Map.of());
    }

    @Test
    public void testDistributionConfig() {
        // Hip Chat public api is currently end of life; need an on premise installation to test
        assumeFalse(HipChatChannel.COMPONENT_NAME.equals(getDescriptor().getName()));

        final FieldAccessor fieldAccessor = createValidFieldAccessor(distribution_config);

        final String destination = createTestConfigDestination();
        try {
            final TestAction descriptorActionApi = getTestAction();
            final TestConfigModel testConfigModel = descriptorActionApi.createTestConfigModel(String.valueOf(distribution_config.getConfigurationId()), fieldAccessor, destination);
            descriptorActionApi.testConfig(testConfigModel);
        } catch (final IntegrationException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGlobalConfig() {
        // Hip Chat public api is currently end of life; need an on premise installation to test
        assumeFalse(HipChatChannel.COMPONENT_NAME.equals(getDescriptor().getName()));
        final ConfigurationModel configurationModel = global_config.orElse(null);
        final FieldAccessor fieldAccessor = createValidFieldAccessor(configurationModel);
        try {
            final TestAction descriptorActionApi = getTestAction();
            descriptorActionApi.testConfig(descriptorActionApi.createTestConfigModel(String.valueOf(configurationModel.getConfigurationId()), fieldAccessor, createTestConfigDestination()));
        } catch (final IntegrationException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCreateChannelEvent() {
        final DistributionEvent channelEvent = createChannelEvent();
        assertEquals(String.valueOf(distribution_config.getConfigurationId()), channelEvent.getConfigId());
        assertEquals(36, channelEvent.getEventId().length());
        assertEquals(destinationName, channelEvent.getDestination());
    }

    @Test
    public void testDistributionValidate() {
        final FieldModel restModel = createValidFieldModel(distribution_config, ConfigContextEnum.DISTRIBUTION);
        final FieldValueModel jobNameField = restModel.getFieldValueModel(ChannelDistributionUIConfig.KEY_NAME).orElseThrow();
        jobNameField.setValue(getTestJobName());
        final HashMap<String, String> fieldErrors = new HashMap<>();
        final Map<String, ConfigField> configFieldMap = createFieldMap(ConfigContextEnum.DISTRIBUTION);
        final FieldValidationAction fieldValidationAction = new FieldValidationAction();
        fieldValidationAction.validateConfig(configFieldMap, restModel, fieldErrors);
        assertTrue(fieldErrors.isEmpty());
    }

    @Test
    public void testDistributionValidateWithFieldErrors() {
        final FieldModel restModel = createInvalidDistributionFieldModel();
        final HashMap<String, String> fieldErrors = new HashMap<>();
        final Map<String, ConfigField> configFieldMap = createFieldMap(ConfigContextEnum.DISTRIBUTION);
        final FieldValidationAction fieldValidationAction = new FieldValidationAction();
        fieldValidationAction.validateConfig(configFieldMap, restModel, fieldErrors);

        if (restModel.getKeyToValues().size() > 0) {
            assertEquals(restModel.getKeyToValues().size(), fieldErrors.size());
        }
    }

    @Test
    public void testGlobalValidate() {
        final FieldModel restModel = createValidFieldModel(global_config.orElse(null), ConfigContextEnum.GLOBAL);
        final HashMap<String, String> fieldErrors = new HashMap<>();
        final Map<String, ConfigField> configFieldMap = createFieldMap(ConfigContextEnum.GLOBAL);
        final FieldValidationAction fieldValidationAction = new FieldValidationAction();
        fieldValidationAction.validateConfig(configFieldMap, restModel, fieldErrors);
        assertTrue(fieldErrors.isEmpty());
    }

    @Test
    public void testGlobalValidateWithFieldErrors() {
        // descriptor has a global configuration therefore continue testing
        final FieldModel restModel = createInvalidGlobalFieldModel();
        final HashMap<String, String> fieldErrors = new HashMap<>();
        final Map<String, ConfigField> configFieldMap = createFieldMap(ConfigContextEnum.GLOBAL);
        final FieldValidationAction fieldValidationAction = new FieldValidationAction();
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
