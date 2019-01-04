package com.synopsys.integration.alert.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.database.BaseDescriptorAccessor;
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationModel;

public abstract class DatabaseConfiguredFieldTest extends AlertIntegrationTest {

    private final List<Descriptor> descriptors;
    @Autowired
    private BaseDescriptorAccessor descriptorAccessor;
    @Autowired
    private BaseConfigurationAccessor configurationAccessor;
    @Autowired
    private DescriptorMap descriptorMap;
    private Set<Long> addedConfigurations;

    public DatabaseConfiguredFieldTest() {
        descriptors = new ArrayList<>();
        descriptors.addAll(descriptorMap.getDescriptorMap().values());
    }

    public DatabaseConfiguredFieldTest(final List<Descriptor> descriptors) {
        this.descriptors = new ArrayList<>();
        descriptors.addAll(descriptors);
    }

    @BeforeAll
    public void initializeDescriptors() throws AlertDatabaseConstraintException {
        for (final Descriptor descriptor : descriptors) {
            registerDescriptor(descriptor);
        }
    }

    @AfterAll
    public void removeDescriptors() throws AlertDatabaseConstraintException {
        for (final Descriptor descriptor : descriptors) {
            unregisterDescriptor(descriptor);
        }
    }

    @BeforeEach
    public void initializeTest() {
        addedConfigurations = new HashSet<>();
    }

    @AfterEach
    public void cleanupDB() throws AlertDatabaseConstraintException {
        for (final Long id : addedConfigurations) {
            deleteConfiguration(id);
        }
    }

    public void registerDescriptor(final Descriptor descriptor) throws AlertDatabaseConstraintException {
        for (final ConfigContextEnum context : descriptor.getAppliedUIContexts()) {
            descriptorAccessor.registerDescriptor(descriptor.getName(), descriptor.getType(), descriptor.getDefinedFields(context));
        }
        descriptors.add(descriptor);
    }

    public void unregisterDescriptor(final Descriptor descriptor) throws AlertDatabaseConstraintException {
        descriptorAccessor.unregisterDescriptor(descriptor.getName());
        descriptors.remove(descriptor);
    }

    public ConfigurationModel addConfiguration(final String descriptorName, final ConfigContextEnum context, final Map<String, Collection<String>> fieldsValues) throws AlertDatabaseConstraintException {
        final Set<ConfigurationFieldModel> fieldModels = fieldsValues
                                                             .entrySet()
                                                             .stream()
                                                             .map(entry -> createConfigurationFieldModel(entry.getKey(), entry.getValue()))
                                                             .collect(Collectors.toSet());
        final ConfigurationModel configurationModel = configurationAccessor.createConfiguration(descriptorName, context, fieldModels);
        addedConfigurations.add(configurationModel.getConfigurationId());
        return configurationModel;
    }

    public ConfigurationFieldModel createConfigurationFieldModel(final String key, final Collection<String> values) {
        final ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(key);
        configurationFieldModel.setFieldValues(values);
        return configurationFieldModel;
    }

    public void deleteConfiguration(final Long id) throws AlertDatabaseConstraintException {
        configurationAccessor.deleteConfiguration(id);
    }

    public BaseDescriptorAccessor getDescriptorAccessor() {
        return descriptorAccessor;
    }

    public BaseConfigurationAccessor getConfigurationAccessor() {
        return configurationAccessor;
    }

    public DescriptorMap getDescriptorMap() {
        return descriptorMap;
    }

    public Set<Long> getAddedConfigurations() {
        return addedConfigurations;
    }

    public List<Descriptor> getDescriptors() {
        return descriptors;
    }
}
