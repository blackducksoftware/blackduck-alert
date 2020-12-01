package com.synopsys.integration.alert.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessorV2;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.configuration.repository.ConfigGroupRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.configuration.repository.FieldValueRepository;
import com.synopsys.integration.alert.database.job.JobConfigurationModelFieldExtractorUtils;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public abstract class DatabaseConfiguredFieldTest extends AlertIntegrationTest {
    @Autowired
    private JobAccessor jobAccessor;
    @Autowired
    private JobAccessorV2 jobAccessorV2;
    @Autowired
    private ConfigurationAccessor configurationAccessor;

    @Autowired
    private DescriptorConfigRepository descriptorConfigRepository;

    @Autowired
    private FieldValueRepository fieldValueRepository;

    @Autowired
    private ConfigGroupRepository configGroupRepository;

    @BeforeEach
    @AfterEach
    public void initializeTest() {
        configGroupRepository.flush();
        descriptorConfigRepository.flush();
        fieldValueRepository.flush();
        configGroupRepository.deleteAllInBatch();
        descriptorConfigRepository.deleteAllInBatch();
        fieldValueRepository.deleteAllInBatch();
    }

    public ConfigurationJobModel addJob(String descriptorName, String providerName, Map<String, Collection<String>> fieldsValues) throws AlertDatabaseConstraintException {
        Map<String, Collection<String>> fieldValuesWithDefaults = createWithDefaults(descriptorName, fieldsValues);

        Set<ConfigurationFieldModel> fieldModels = fieldValuesWithDefaults
                                                       .entrySet()
                                                       .stream()
                                                       .map(entry -> createConfigurationFieldModel(entry.getKey(), entry.getValue()))
                                                       .collect(Collectors.toSet());
        ConfigurationJobModel configurationJobModel = jobAccessor.createJob(Set.of(descriptorName, providerName), fieldModels);
        return configurationJobModel;
    }

    public DistributionJobModel addDistributionJob(String descriptorName, Map<String, Collection<String>> fieldsValues) throws AlertException {
        Map<String, Collection<String>> fieldValuesWithDefaults = createWithDefaults(descriptorName, fieldsValues);
        Map<String, ConfigurationFieldModel> keyToConfigurationFieldModels = new HashMap<>();
        for (Map.Entry<String, Collection<String>> fieldToValues : fieldValuesWithDefaults.entrySet()) {
            String fieldKey = fieldToValues.getKey();
            ConfigurationFieldModel fieldModel = ConfigurationFieldModel.create(fieldKey);
            fieldModel.setFieldValues(fieldToValues.getValue());
            keyToConfigurationFieldModels.put(fieldKey, fieldModel);
        }

        DistributionJobModel initialJobModel = JobConfigurationModelFieldExtractorUtils.convertToDistributionJobModel(null, keyToConfigurationFieldModels, DateUtils.createCurrentDateTimestamp(), null);
        DistributionJobRequestModel jobRequestModel = createDistributionJobRequestModel(initialJobModel);
        return jobAccessorV2.createJob(jobRequestModel);
    }

    public ConfigurationModel addGlobalConfiguration(DescriptorKey descriptorKey, Map<String, Collection<String>> fieldsValues) {
        Set<ConfigurationFieldModel> fieldModels = fieldsValues
                                                       .entrySet()
                                                       .stream()
                                                       .map(entry -> createConfigurationFieldModel(entry.getKey(), entry.getValue()))
                                                       .collect(Collectors.toSet());

        return configurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.GLOBAL, fieldModels);
    }

    public ConfigurationFieldModel createConfigurationFieldModel(String key, Collection<String> values) {
        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(key);
        configurationFieldModel.setFieldValues(values);
        return configurationFieldModel;
    }

    public ConfigurationAccessor getConfigurationAccessor() {
        return configurationAccessor;
    }

    private Map<String, Collection<String>> createWithDefaults(String descriptorName, Map<String, Collection<String>> initialFieldsValues) {
        Map<String, Collection<String>> fieldValuesWithDefaults = new HashMap<>();
        fieldValuesWithDefaults.putIfAbsent(ChannelDistributionUIConfig.KEY_FREQUENCY, List.of(FrequencyType.DAILY.name()));
        fieldValuesWithDefaults.putIfAbsent(ProviderDistributionUIConfig.KEY_PROCESSING_TYPE, List.of(ProcessingType.DEFAULT.name()));
        fieldValuesWithDefaults.putIfAbsent(ChannelDistributionUIConfig.KEY_CHANNEL_NAME, List.of(descriptorName));
        fieldValuesWithDefaults.putAll(initialFieldsValues);
        return fieldValuesWithDefaults;
    }

    private DistributionJobRequestModel createDistributionJobRequestModel(DistributionJobModel initialJobModel) {
        return new DistributionJobRequestModel(
            initialJobModel.isEnabled(),
            initialJobModel.getName(),
            initialJobModel.getDistributionFrequency(),
            initialJobModel.getProcessingType(),
            initialJobModel.getChannelDescriptorName(),
            initialJobModel.getBlackDuckGlobalConfigId(),
            initialJobModel.isFilterByProject(),
            initialJobModel.getProjectNamePattern().orElse(null),
            initialJobModel.getNotificationTypes(),
            initialJobModel.getProjectFilterProjectNames(),
            initialJobModel.getPolicyFilterPolicyNames(),
            initialJobModel.getVulnerabilityFilterSeverityNames(),
            initialJobModel.getDistributionJobDetails()
        );
    }

}
