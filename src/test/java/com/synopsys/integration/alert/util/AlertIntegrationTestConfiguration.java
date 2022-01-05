package com.synopsys.integration.alert.util;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.database.configuration.repository.ConfigContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.DefinedFieldRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorFieldRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorTypeRepository;
import com.synopsys.integration.alert.database.configuration.repository.FieldContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.alert.test.common.TestPropertyKey;

@TestConfiguration
public class AlertIntegrationTestConfiguration {
    @Autowired
    private DescriptorTypeRepository descriptorTypeRepository;
    @Autowired
    private RegisteredDescriptorRepository registeredDescriptorRepository;
    @Autowired
    private DescriptorFieldRepository descriptorFieldRepository;
    @Autowired
    private DefinedFieldRepository definedFieldRepository;
    @Autowired
    private FieldContextRepository fieldContextRepository;
    @Autowired
    private ConfigContextRepository configContextRepository;
    @Autowired
    private ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;

    @Bean
    @DependsOn({ "descriptorTypeRepository", "registeredDescriptorRepository", "descriptorFieldRepository", "definedFieldRepository", "fieldContextRepository", "configContextRepository" })
    public DescriptorMocker descriptorMocker() {
        return new DescriptorMocker(descriptorTypeRepository, registeredDescriptorRepository, descriptorFieldRepository, definedFieldRepository, fieldContextRepository, configContextRepository);
    }

    // FIXME delete
    @Deprecated(forRemoval = true)
    public void initBlackDuckData() throws AlertException {
        TestProperties testProperties = new TestProperties();

        ConfigurationFieldModel blackDuckURLField = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_URL);
        blackDuckURLField.setFieldValue(testProperties.getBlackDuckURL());

        ConfigurationFieldModel blackDuckAPITokenField = ConfigurationFieldModel.createSensitive(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY);
        blackDuckAPITokenField.setFieldValue(testProperties.getBlackDuckAPIToken());

        ConfigurationFieldModel blackDuckTimeoutField = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT);
        blackDuckTimeoutField.setFieldValue(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_TIMEOUT));

        BlackDuckProviderKey blackDuckProviderKey = new BlackDuckProviderKey();
        ConfigurationModel blackDuckConfiguration = configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(blackDuckProviderKey, ConfigContextEnum.GLOBAL).stream().findFirst()
                                                        .orElse(
                                                            configurationModelConfigurationAccessor.createConfiguration(blackDuckProviderKey, ConfigContextEnum.GLOBAL, List.of(blackDuckURLField, blackDuckAPITokenField, blackDuckTimeoutField))
                                                        );
        configurationModelConfigurationAccessor.updateConfiguration(blackDuckConfiguration.getConfigurationId(), List.of(blackDuckURLField, blackDuckAPITokenField, blackDuckTimeoutField));
    }

}
