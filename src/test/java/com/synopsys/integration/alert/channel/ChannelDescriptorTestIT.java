package com.synopsys.integration.alert.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.channel.ChannelDistributionTestAction;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.DescriptorProcessor;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.FieldValidationUtility;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessorV2;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;
import com.synopsys.integration.alert.common.util.DataStructureUtils;
import com.synopsys.integration.alert.database.api.DefaultConfigurationAccessor;
import com.synopsys.integration.alert.database.api.DefaultDescriptorAccessor;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.descriptor.api.model.ProviderKey;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.web.api.job.JobFieldModelPopulationUtils;
import com.synopsys.integration.exception.IntegrationException;

@Transactional
public abstract class ChannelDescriptorTestIT extends AlertIntegrationTest {
    protected Gson gson;
    protected DistributionEvent channelEvent;

    protected ContentConverter contentConverter;
    protected TestProperties testProperties;

    @Autowired
    protected ProviderKey providerKey;
    @Autowired
    protected JobAccessorV2 jobAccessor;
    @Autowired
    protected DefaultConfigurationAccessor configurationAccessor;
    @Autowired
    protected DefaultDescriptorAccessor descriptorAccessor;
    @Autowired
    protected DescriptorProcessor descriptorProcessor;
    @Autowired
    protected RegisteredDescriptorRepository registeredDescriptorRepository;

    protected ConfigurationModel providerGlobalConfig;
    protected Optional<ConfigurationModel> optionalChannelGlobalConfig;
    protected DistributionJobModel distributionJobModel;
    protected String eventDestinationName;

    @BeforeEach
    public void init() throws Exception {
        gson = new Gson();
        contentConverter = new ContentConverter(gson, new DefaultConversionService());
        testProperties = new TestProperties();
        providerGlobalConfig = saveProviderGlobalConfig();
        optionalChannelGlobalConfig = saveGlobalConfiguration();
        eventDestinationName = getEventDestinationName();
        channelEvent = createChannelEvent();
        distributionJobModel = saveDistributionJob();
    }

    @AfterEach
    public void cleanupTest() {
        if (null != optionalChannelGlobalConfig && optionalChannelGlobalConfig.isPresent()) {
            configurationAccessor.deleteConfiguration(optionalChannelGlobalConfig.get());
        }

        if (distributionJobModel != null) {
            try {
                jobAccessor.deleteJob(distributionJobModel.getJobId());
            } catch (AlertDatabaseConstraintException e) {
                // TODO delete this try-catch when method signature no longer contains that exception
            }
        }

        if (providerGlobalConfig != null) {
            configurationAccessor.deleteConfiguration(providerGlobalConfig);
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

    private FieldModel createValidFieldModel(ConfigurationModel configurationModel, ConfigContextEnum context) {
        Map<String, FieldValueModel> fieldValueMap = createFieldModelMap(configurationModel.getCopyOfFieldList());
        if (ConfigContextEnum.DISTRIBUTION == context) {
            optionalChannelGlobalConfig.ifPresent(globalConfig -> fieldValueMap.putAll(createFieldModelMap(globalConfig.getCopyOfFieldList())));
        }
        FieldModel model = new FieldModel(String.valueOf(configurationModel.getConfigurationId()), eventDestinationName, context.name(), fieldValueMap);
        return model;
    }

    public FieldUtility createValidGlobalFieldUtility(ConfigurationModel configurationModel) {
        Map<String, ConfigurationFieldModel> fieldMap = new HashMap<>();
        fieldMap.putAll(configurationModel.getCopyOfKeyToFieldMap());
        optionalChannelGlobalConfig.ifPresent(globalConfig -> fieldMap.putAll(globalConfig.getCopyOfKeyToFieldMap()));
        return new FieldUtility(fieldMap);

    }

    public FieldModel createInvalidGlobalFieldModel() {
        Map<String, String> invalidValuesMap = createInvalidGlobalFieldMap();
        Map<String, FieldValueModel> fieldModelMap = createFieldValueModelMap(invalidValuesMap);
        FieldModel model = new FieldModel("1L", eventDestinationName, ConfigContextEnum.GLOBAL.name(), fieldModelMap);
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

    public abstract DistributionJobDetailsModel createDistributionJobDetails();

    public abstract ChannelDescriptor getDescriptor();

    public abstract boolean assertGlobalFields(Set<DefinedFieldModel> globalFields);

    public abstract boolean assertDistributionFields(Set<DefinedFieldModel> distributionFields);

    public abstract FieldModel createTestConfigDestination();

    public abstract Map<String, String> createInvalidGlobalFieldMap();

    public abstract String getTestJobName();

    public abstract String getEventDestinationName();

    public abstract TestAction getGlobalTestAction();

    public abstract ChannelDistributionTestAction getChannelDistributionTestAction();

    protected ConfigurationModel saveProviderGlobalConfig() {
        return configurationAccessor.createConfiguration(providerKey, ConfigContextEnum.GLOBAL, List.of());
    }

    private DistributionJobModel saveDistributionJob() {
        DistributionJobRequestModel requestModel = new DistributionJobRequestModel(
            true,
            getClass().getSimpleName(),
            FrequencyType.REAL_TIME,
            ProcessingType.DEFAULT,
            eventDestinationName,
            providerGlobalConfig.getConfigurationId(),
            false,
            null,
            List.of("VULNERABILITY"),
            List.of(),
            List.of(),
            List.of(),
            createDistributionJobDetails()
        );

        try {
            return jobAccessor.createJob(requestModel);
        } catch (AlertDatabaseConstraintException e) {
            // TODO remove this when method signature is updated
            throw new AlertRuntimeException(e);
        }
    }

    private Map<String, ConfigField> createFieldMap(ConfigContextEnum context) {
        return getDescriptor().getUIConfig(context)
                   .map(uiConfig -> DataStructureUtils.mapToValues(uiConfig.getFields(), ConfigField::getKey))
                   .orElse(Map.of());
    }

    @Test
    public void testDistributionConfig() {
        try {
            ChannelDistributionTestAction descriptorActionApi = getChannelDistributionTestAction();
            descriptorActionApi.testConfig(distributionJobModel, optionalChannelGlobalConfig.orElse(null), "Topic - Channel Descriptor Test IT", "Message - Channel Descriptor Test IT", null);
        } catch (IntegrationException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testGlobalConfig() {
        assumeTrue(optionalChannelGlobalConfig.isPresent(), "Cannot test channel global configuration because none was provided to test");
        ConfigurationModel channelGlobalConfig = optionalChannelGlobalConfig.get();
        FieldUtility fieldUtility = createValidGlobalFieldUtility(channelGlobalConfig);
        try {
            TestAction globalConfigTestAction = getGlobalTestAction();
            globalConfigTestAction.testConfig(String.valueOf(channelGlobalConfig.getConfigurationId()), createTestConfigDestination(), fieldUtility);
        } catch (IntegrationException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testCreateChannelEvent() throws Exception {
        DistributionEvent channelEvent = createChannelEvent();
        assertEquals(36, channelEvent.getEventId().length());
        assertEquals(eventDestinationName, channelEvent.getDestination());
    }

    @Test
    public void testDistributionValidate() {
        JobFieldModel jobFieldModel = JobFieldModelPopulationUtils.createJobFieldModel(distributionJobModel);

        Map<String, ConfigField> configFields = new HashMap<>();
        for (FieldModel singleFieldModelFromJob : jobFieldModel.getFieldModels()) {
            List<ConfigField> fieldsFromModel = descriptorProcessor.retrieveUIConfigFields(singleFieldModelFromJob.getContext(), singleFieldModelFromJob.getDescriptorName());
            for (ConfigField fieldFromModel : fieldsFromModel) {
                String fieldKey = fieldFromModel.getKey();
                if (!configFields.containsKey(fieldKey)) {
                    configFields.put(fieldKey, fieldFromModel);
                }
            }
        }

        FieldValidationUtility fieldValidationAction = new FieldValidationUtility();
        List<AlertFieldStatus> fieldErrors = fieldValidationAction.validateConfig(configFields, jobFieldModel.getFieldModels());
        assertTrue(fieldErrors.isEmpty());
    }

    @Test
    public void testDistributionValidateWithFieldErrors() {
        ConfigContextEnum context = ConfigContextEnum.DISTRIBUTION;
        FieldModel restModel = new FieldModel(eventDestinationName, context.name(), Map.of());
        Map<String, ConfigField> configFieldMap = createFieldMap(context);
        FieldValidationUtility fieldValidationAction = new FieldValidationUtility();
        List<AlertFieldStatus> fieldErrors = fieldValidationAction.validateConfig(configFieldMap, restModel);

        if (restModel.getKeyToValues().size() > 0) {
            assertEquals(restModel.getKeyToValues().size(), fieldErrors.size());
        }
    }

    @Test
    public void testGlobalValidate() {
        ConfigurationModel globalChannelConfig = optionalChannelGlobalConfig
                                                     .orElseThrow(() -> new AlertRuntimeException("Missing global channel config"));
        FieldModel restModel = createValidFieldModel(globalChannelConfig, ConfigContextEnum.GLOBAL);
        Map<String, ConfigField> configFieldMap = createFieldMap(ConfigContextEnum.GLOBAL);
        FieldValidationUtility fieldValidationAction = new FieldValidationUtility();
        List<AlertFieldStatus> fieldErrors = fieldValidationAction.validateConfig(configFieldMap, restModel);

        assertTrue(fieldErrors.isEmpty(), StringUtils.join(fieldErrors, "; "));
    }

    @Test
    public void testGlobalValidateWithFieldErrors() {
        // descriptor has a global configuration therefore continue testing
        FieldModel restModel = createInvalidGlobalFieldModel();
        Map<String, ConfigField> configFieldMap = createFieldMap(ConfigContextEnum.GLOBAL);
        FieldValidationUtility fieldValidationAction = new FieldValidationUtility();
        List<AlertFieldStatus> fieldErrors = fieldValidationAction.validateConfig(configFieldMap, restModel);
        assertEquals(restModel.getKeyToValues().size(), fieldErrors.size());
    }

    @Test
    public void testDefinedFields() {
        assertTrue(assertGlobalFields(getDescriptor().getAllDefinedFields(ConfigContextEnum.GLOBAL)));
        assertTrue(assertDistributionFields(getDescriptor().getAllDefinedFields(ConfigContextEnum.DISTRIBUTION)));
        assertTrue(getDescriptor().getAllDefinedFields(null).isEmpty());
    }

}
