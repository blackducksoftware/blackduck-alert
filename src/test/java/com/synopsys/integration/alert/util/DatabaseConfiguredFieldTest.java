package com.synopsys.integration.alert.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.database.configuration.repository.ConfigGroupRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.configuration.repository.FieldValueRepository;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public abstract class DatabaseConfiguredFieldTest extends AlertIntegrationTest {
    private final List<Descriptor> descriptors = new LinkedList<>();
    @Autowired
    private DescriptorAccessor descriptorAccessor;
    @Autowired
    private DescriptorMocker descriptorMocker;
    @Autowired
    private JobAccessor jobAccessor;
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
        Map<String, Collection<String>> fieldValuesWithDefaults = new HashMap<>();
        fieldValuesWithDefaults.put(ChannelDistributionUIConfig.KEY_FREQUENCY, List.of(FrequencyType.DAILY.name()));
        fieldValuesWithDefaults.put(ProviderDistributionUIConfig.KEY_PROCESSING_TYPE, List.of(ProcessingType.DEFAULT.name()));
        fieldValuesWithDefaults.put(ChannelDistributionUIConfig.KEY_CHANNEL_NAME, List.of(descriptorName));
        fieldValuesWithDefaults.putAll(fieldsValues);

        Set<ConfigurationFieldModel> fieldModels = fieldValuesWithDefaults
                                                       .entrySet()
                                                       .stream()
                                                       .map(entry -> createConfigurationFieldModel(entry.getKey(), entry.getValue()))
                                                       .collect(Collectors.toSet());
        ConfigurationJobModel configurationJobModel = jobAccessor.createJob(Set.of(descriptorName, providerName), fieldModels);
        return configurationJobModel;
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

    protected JobAccessor getJobAccessor() {
        return jobAccessor;
    }

    public ConfigurationAccessor getConfigurationAccessor() {
        return configurationAccessor;
    }

}
