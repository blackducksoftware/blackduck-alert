package com.synopsys.integration.alert.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import com.synopsys.integration.alert.database.api.configuration.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.DefinedFieldModel;
import com.synopsys.integration.alert.database.api.configuration.DescriptorAccessor;
import com.synopsys.integration.alert.util.TestProperties;
import com.synopsys.integration.alert.web.model.FieldModel;
import com.synopsys.integration.alert.web.model.FieldValueModel;
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

    protected ConfigurationAccessor.ConfigurationModel provider_global;
    protected Optional<ConfigurationAccessor.ConfigurationModel> global_config;
    protected ConfigurationAccessor.ConfigurationModel distribution_config;

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
        final List<DescriptorAccessor.RegisteredDescriptorModel> registeredDescriptorModels = descriptorAccessor.getRegisteredDescriptors();

        for (final DescriptorAccessor.RegisteredDescriptorModel registeredDescriptor : registeredDescriptorModels) {
            try {
                descriptorAccessor.unregisterDescriptor(registeredDescriptor.getName());
            } catch (final AlertDatabaseConstraintException ex) {
                ex.printStackTrace();
            }
        }
    }

    public Map<String, FieldValueModel> createFieldModelMap() {
        final List<ConfigurationFieldModel> configModels = new LinkedList<>();
        configModels.addAll(distribution_config.getCopyOfFieldList());
        global_config.ifPresent(model -> configModels.addAll(model.getCopyOfFieldList()));

        final Map<String, FieldValueModel> fieldModelMap = new HashMap<>();
        for (final ConfigurationFieldModel model : configModels) {
            final String key = model.getFieldKey();
            Collection<String> values = Collections.emptyList();
            if (!model.isSensitive()) {
                values = model.getFieldValues();
            }
            final FieldValueModel fieldValueModel = new FieldValueModel(values, model.isSet());
            fieldModelMap.put(key, fieldValueModel);
        }
        return fieldModelMap;
    }

    public FieldModel createValidFieldModel() {
        final Map<String, FieldValueModel> valueMap = createFieldModelMap();
        final FieldModel model = new FieldModel(String.valueOf(distribution_config.getConfigurationId()), getDescriptor().getDestinationName(), ConfigContextEnum.DISTRIBUTION.name(), valueMap);
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
        final Map<String, String> invalidValuesMap = Map.of(CommonDistributionUIConfig.KEY_NAME, "",
            CommonDistributionUIConfig.KEY_CHANNEL_NAME, "",
            CommonDistributionUIConfig.KEY_PROVIDER_NAME, "");
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

    public abstract Optional<ConfigurationAccessor.ConfigurationModel> saveGlobalConfiguration() throws Exception;

    public abstract ConfigurationAccessor.ConfigurationModel saveDistributionConfiguration() throws Exception;

    public abstract ChannelDescriptor getDescriptor();

    public abstract boolean assertGlobalFields(Collection<DefinedFieldModel> globalFields);

    public abstract boolean assertDistributionFields(Collection<DefinedFieldModel> distributionFields);

    public abstract Map<String, String> createInvalidGlobalFieldMap();

    public abstract Map<String, String> createInvalidDistributionFieldMap();

    @Test
    public void testSendTestMessage() throws Exception {
        final DescriptorActionApi descriptorActionApi = getDescriptor().getRestApi(ConfigContextEnum.DISTRIBUTION);
        final FieldModel restModel = createValidFieldModel();
        try {
            descriptorActionApi.testConfig(descriptorActionApi.createTestConfigModel(restModel, getDescriptor().getDestinationName()));
        } catch (final IntegrationException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCreateChannelEvent() {
        final DistributionEvent channelEvent = this.createChannelEvent();
        assertEquals(String.valueOf(distribution_config.getConfigurationId()), channelEvent.getConfigId());
        assertEquals(36, channelEvent.getEventId().length());
        assertEquals(getDescriptor().getDestinationName(), channelEvent.getDestination());
    }

    @Test
    public void testDistributionValidate() {
        final DescriptorActionApi descriptorActionApi = getDescriptor().getRestApi(ConfigContextEnum.DISTRIBUTION);
        final FieldModel restModel = createValidFieldModel();
        final HashMap<String, String> fieldErrors = new HashMap<>();
        descriptorActionApi.validateConfig(restModel.convertToFieldAccessor(), fieldErrors);
        assertTrue(fieldErrors.isEmpty());
    }

    @Test
    public void testDistributionValidateWithFieldErrors() {
        final DescriptorActionApi descriptorActionApi = getDescriptor().getRestApi(ConfigContextEnum.DISTRIBUTION);
        final FieldModel restModel = createInvalidDistributionFieldModel();
        final HashMap<String, String> fieldErrors = new HashMap<>();
        descriptorActionApi.validateConfig(restModel.convertToFieldAccessor(), fieldErrors);
        assertEquals(restModel.getKeyToValues().size(), fieldErrors.size());
    }

    @Test
    public void testGlobalValidate() {
        final DescriptorActionApi descriptorActionApi = getDescriptor().getRestApi(ConfigContextEnum.GLOBAL);
        assumeTrue(null != descriptorActionApi);
        final FieldModel restModel = createValidFieldModel();
        final HashMap<String, String> fieldErrors = new HashMap<>();
        descriptorActionApi.validateConfig(restModel.convertToFieldAccessor(), fieldErrors);
        assertTrue(fieldErrors.isEmpty());
    }

    @Test
    public void testGlobalValidateWithFieldErrors() {
        final DescriptorActionApi descriptorActionApi = getDescriptor().getRestApi(ConfigContextEnum.GLOBAL);
        assumeTrue(null != descriptorActionApi);
        // descriptor has a global configuration therefore continue testing
        final FieldModel restModel = createInvalidGlobalFieldModel();
        final HashMap<String, String> fieldErrors = new HashMap<>();
        descriptorActionApi.validateConfig(restModel.convertToFieldAccessor(), fieldErrors);
        assertEquals(restModel.getKeyToValues().size(), fieldErrors.size());
    }

    @Test
    public void testDefinedFields() {
        assertTrue(assertGlobalFields(getDescriptor().getDefinedFields(ConfigContextEnum.GLOBAL)));
        assertTrue(assertDistributionFields(getDescriptor().getDefinedFields(ConfigContextEnum.DISTRIBUTION)));
        assertTrue(getDescriptor().getDefinedFields(null).isEmpty());
    }
}
