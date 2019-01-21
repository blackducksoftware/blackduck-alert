package com.synopsys.integration.alert.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.DefaultConversionService;

import com.google.gson.Gson;
import com.synopsys.integration.alert.FieldRegistrationIntegrationTest;
import com.synopsys.integration.alert.channel.event.DistributionEvent;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.context.DescriptorActionApi;
import com.synopsys.integration.alert.common.descriptor.config.ui.CommonDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor;
import com.synopsys.integration.alert.database.api.configuration.DescriptorAccessor;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationModel;
import com.synopsys.integration.alert.database.api.configuration.model.DefinedFieldModel;
import com.synopsys.integration.alert.database.api.configuration.model.RegisteredDescriptorModel;
import com.synopsys.integration.alert.util.TestProperties;
import com.synopsys.integration.alert.web.model.configuration.FieldModel;
import com.synopsys.integration.alert.web.model.configuration.FieldValueModel;
import com.synopsys.integration.exception.IntegrationException;

public abstract class ChannelDescriptorTest extends FieldRegistrationIntegrationTest {
    protected Gson gson;
    protected DistributionEvent channelEvent;

    protected ContentConverter contentConverter;
    protected TestProperties properties;

    @Autowired
    protected ConfigurationAccessor configurationAccessor;

    @Autowired
    protected DescriptorAccessor descriptorAccessor;

    protected ConfigurationModel provider_global;
    protected Optional<ConfigurationModel> global_config;
    protected ConfigurationModel distribution_config;

    @BeforeEach
    public void init() throws Exception {
        registerDescriptors();
        gson = new Gson();
        contentConverter = new ContentConverter(gson, new DefaultConversionService());
        properties = new TestProperties();
        global_config = saveGlobalConfiguration();
        distribution_config = saveDistributionConfiguration();
        channelEvent = createChannelEvent();
    }

    @AfterEach
    public void cleanupTest() throws Exception {
        if (global_config.isPresent()) {
            configurationAccessor.deleteConfiguration(global_config.get());
        }

        if (distribution_config != null) {
            configurationAccessor.deleteConfiguration(distribution_config);
        }

        if (provider_global != null) {
            configurationAccessor.deleteConfiguration(provider_global);
        }
        final List<RegisteredDescriptorModel> registeredDescriptorModels = descriptorAccessor.getRegisteredDescriptors();

        for (final RegisteredDescriptorModel registeredDescriptor : registeredDescriptorModels) {
            try {
                descriptorAccessor.unregisterDescriptor(registeredDescriptor.getName());
            } catch (final AlertDatabaseConstraintException ex) {
                ex.printStackTrace();
            }
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
        final FieldModel model = new FieldModel(String.valueOf(configurationModel.getConfigurationId()), getDescriptor().getDestinationName(), context.name(), fieldValueMap);
        return model;

    }

    public FieldModel createInvalidDistributionFieldModel() {
        final Map<String, String> invalidValuesMap = new HashMap<>();
        invalidValuesMap.putAll(createInvalidCommonDistributionFieldMap());
        invalidValuesMap.putAll(createInvalidDistributionFieldMap());

        final Map<String, FieldValueModel> fieldModelMap = createFieldValueModelMap(invalidValuesMap);
        final FieldModel model = new FieldModel("1L", getDescriptor().getDestinationName(), ConfigContextEnum.DISTRIBUTION.name(), fieldModelMap);
        return model;
    }

    public Map<String, String> createInvalidCommonDistributionFieldMap() {
        final Map<String, String> invalidValuesMap = Map.of();
        return invalidValuesMap;
    }

    public FieldModel createInvalidGlobalFieldModel() {
        final Map<String, String> invalidValuesMap = createInvalidGlobalFieldMap();
        final Map<String, FieldValueModel> fieldModelMap = createFieldValueModelMap(invalidValuesMap);
        final FieldModel model = new FieldModel("1L", getDescriptor().getDestinationName(), ConfigContextEnum.GLOBAL.name(), fieldModelMap);
        return model;
    }

    public Map<String, FieldValueModel> createFieldValueModelMap(final Map<String, String> fieldValueMap) {
        final Map<String, FieldValueModel> fieldModelMap = new HashMap<>();
        for (final Map.Entry<String, String> fieldValue : fieldValueMap.entrySet()) {
            final String key = fieldValue.getKey();
            final FieldValueModel fieldValueModel = new FieldValueModel(List.of(fieldValue.getValue()), true);
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

    @Test
    public void testDistributionConfig() {
        final Optional<DescriptorActionApi> descriptorActionApi = getDescriptor().getActionApi(ConfigContextEnum.DISTRIBUTION);
        final FieldModel restModel = createValidFieldModel(distribution_config, ConfigContextEnum.DISTRIBUTION);
        final FieldValueModel jobNameField = restModel.getField(CommonDistributionUIConfig.KEY_NAME).orElseThrow();
        jobNameField.setValue(getTestJobName());
        try {
            assertTrue(descriptorActionApi.isPresent());
            descriptorActionApi.get().testConfig(getDescriptor().getUIConfig(ConfigContextEnum.DISTRIBUTION).get().createFields(), descriptorActionApi.get().createTestConfigModel(restModel, createTestConfigDestination()));
        } catch (final IntegrationException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGlobalConfig() {
        final Optional<DescriptorActionApi> descriptorActionApi = getDescriptor().getActionApi(ConfigContextEnum.GLOBAL);
        assumeTrue(descriptorActionApi.isPresent());
        final FieldModel restModel = createValidFieldModel(global_config.orElse(null), ConfigContextEnum.GLOBAL);
        try {
            assertTrue(descriptorActionApi.isPresent());
            descriptorActionApi.get().testConfig(getDescriptor().getUIConfig(ConfigContextEnum.DISTRIBUTION).get().createFields(), descriptorActionApi.get().createTestConfigModel(restModel, createTestConfigDestination()));
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
        assertEquals(getDescriptor().getDestinationName(), channelEvent.getDestination());
    }

    @Test
    public void testDistributionValidate() throws Exception {
        final Optional<DescriptorActionApi> descriptorActionApi = getDescriptor().getActionApi(ConfigContextEnum.DISTRIBUTION);
        final FieldModel restModel = createValidFieldModel(distribution_config, ConfigContextEnum.DISTRIBUTION);
        final FieldValueModel jobNameField = restModel.getField(CommonDistributionUIConfig.KEY_NAME).orElseThrow();
        jobNameField.setValue(getTestJobName());
        final HashMap<String, String> fieldErrors = new HashMap<>();
        final List<ConfigurationModel> models = configurationAccessor.getConfigurationsByDescriptorName(getDescriptor().getName());
        assertTrue(descriptorActionApi.isPresent());
        descriptorActionApi.get().validateConfig(getDescriptor().getUIConfig(ConfigContextEnum.DISTRIBUTION).get().createFields(), restModel, fieldErrors);
        assertTrue(fieldErrors.isEmpty());
    }

    @Test
    public void testDistributionValidateWithFieldErrors() {
        final Optional<DescriptorActionApi> descriptorActionApi = getDescriptor().getActionApi(ConfigContextEnum.DISTRIBUTION);
        final FieldModel restModel = createInvalidDistributionFieldModel();
        final HashMap<String, String> fieldErrors = new HashMap<>();
        assertTrue(descriptorActionApi.isPresent());
        descriptorActionApi.get().validateConfig(getDescriptor().getUIConfig(ConfigContextEnum.DISTRIBUTION).get().createFields(), restModel, fieldErrors);

        if (restModel.getKeyToValues().size() > 0) {
            assertEquals(restModel.getKeyToValues().size(), fieldErrors.size());
        }
    }

    @Test
    public void testGlobalValidate() {
        final Optional<DescriptorActionApi> descriptorActionApi = getDescriptor().getActionApi(ConfigContextEnum.GLOBAL);
        assumeTrue(descriptorActionApi.isPresent());
        final FieldModel restModel = createValidFieldModel(global_config.orElse(null), ConfigContextEnum.GLOBAL);
        final HashMap<String, String> fieldErrors = new HashMap<>();
        assertTrue(descriptorActionApi.isPresent());
        descriptorActionApi.get().validateConfig(getDescriptor().getUIConfig(ConfigContextEnum.GLOBAL).get().createFields(), restModel, fieldErrors);
        assertTrue(fieldErrors.isEmpty());
    }

    @Test
    public void testGlobalValidateWithFieldErrors() {
        final Optional<DescriptorActionApi> descriptorActionApi = getDescriptor().getActionApi(ConfigContextEnum.GLOBAL);
        assumeTrue(descriptorActionApi.isPresent());
        // descriptor has a global configuration therefore continue testing
        final FieldModel restModel = createInvalidGlobalFieldModel();
        final HashMap<String, String> fieldErrors = new HashMap<>();
        assertTrue(descriptorActionApi.isPresent());
        descriptorActionApi.get().validateConfig(getDescriptor().getUIConfig(ConfigContextEnum.GLOBAL).get().createFields(), restModel, fieldErrors);
        assertEquals(restModel.getKeyToValues().size(), fieldErrors.size());
    }

    @Test
    public void testDefinedFields() {
        assertTrue(assertGlobalFields(getDescriptor().getAllDefinedFields(ConfigContextEnum.GLOBAL)));
        assertTrue(assertDistributionFields(getDescriptor().getAllDefinedFields(ConfigContextEnum.DISTRIBUTION)));
        assertTrue(getDescriptor().getAllDefinedFields(null).isEmpty());
    }
}
