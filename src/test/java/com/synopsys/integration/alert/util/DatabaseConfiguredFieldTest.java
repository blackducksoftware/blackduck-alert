package com.synopsys.integration.alert.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.database.BaseDescriptorAccessor;
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationJobModel;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationModel;
import com.synopsys.integration.alert.database.repository.configuration.ConfigGroupRepository;
import com.synopsys.integration.alert.database.repository.configuration.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.repository.configuration.FieldValueRepository;

public abstract class DatabaseConfiguredFieldTest extends AlertIntegrationTest {

    private final List<Descriptor> descriptors = new LinkedList<>();
    @Autowired
    private BaseDescriptorAccessor descriptorAccessor;
    @Autowired
    private BaseConfigurationAccessor configurationAccessor;
    private Set<String> addedConfigurations;

    @Autowired
    private DescriptorConfigRepository descriptorConfigRepository;

    @Autowired
    private FieldValueRepository fieldValueRepository;

    @Autowired
    private ConfigGroupRepository configGroupRepository;

    @BeforeEach
    @AfterEach
    public void initializeTest() {
        addedConfigurations = new HashSet<>();
        configGroupRepository.deleteAllInBatch();
        descriptorConfigRepository.deleteAllInBatch();
        fieldValueRepository.deleteAllInBatch();
    }

    @AfterEach
    public void cleanupDB() throws AlertDatabaseConstraintException {
        for (final String id : addedConfigurations) {
            deleteConfiguration(id);
        }
    }

    public ConfigurationJobModel addJob(String descriptorName, String providerName, final Map<String, Collection<String>> fieldsValues) throws AlertDatabaseConstraintException {
        final Set<ConfigurationFieldModel> fieldModels = fieldsValues
                                                             .entrySet()
                                                             .stream()
                                                             .map(entry -> createConfigurationFieldModel(entry.getKey(), entry.getValue()))
                                                             .collect(Collectors.toSet());
        final ConfigurationJobModel configurationJobModel = configurationAccessor.createJob(Set.of(descriptorName, providerName), fieldModels);
        addedConfigurations.add(configurationJobModel.getJobId().toString());
        return configurationJobModel;
    }

    public ConfigurationModel addConfiguration(final String descriptorName, final ConfigContextEnum context, final Map<String, Collection<String>> fieldsValues) throws AlertDatabaseConstraintException {
        final Set<ConfigurationFieldModel> fieldModels = fieldsValues
                                                             .entrySet()
                                                             .stream()
                                                             .map(entry -> createConfigurationFieldModel(entry.getKey(), entry.getValue()))
                                                             .collect(Collectors.toSet());
        final ConfigurationModel configurationModel = configurationAccessor.createConfiguration(descriptorName, context, fieldModels);
        addedConfigurations.add(String.valueOf(configurationModel.getConfigurationId()));
        return configurationModel;
    }

    public ConfigurationFieldModel createConfigurationFieldModel(final String key, final Collection<String> values) {
        final ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(key);
        configurationFieldModel.setFieldValues(values);
        return configurationFieldModel;
    }

    public void deleteConfiguration(final String id) throws AlertDatabaseConstraintException {
        try {
            final long longId = Long.parseLong(id);
            if (configurationAccessor.getConfigurationById(longId).isPresent()) {
                configurationAccessor.deleteConfiguration(longId);
            }
        } catch (NumberFormatException e) {
            UUID uuid = UUID.fromString(id);
            if (configurationAccessor.getJobById(uuid).isPresent()) {
                configurationAccessor.deleteJob(uuid);
            }
        }
    }

    public BaseDescriptorAccessor getDescriptorAccessor() {
        return descriptorAccessor;
    }

    public BaseConfigurationAccessor getConfigurationAccessor() {
        return configurationAccessor;
    }

    public Set<String> getAddedConfigurations() {
        return addedConfigurations;
    }

    public List<Descriptor> getDescriptors() {
        return descriptors;
    }
}
