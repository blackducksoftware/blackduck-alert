package com.synopsys.integration.alert.util;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.database.configuration.repository.ConfigContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.DefinedFieldRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorFieldRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorTypeRepository;
import com.synopsys.integration.alert.database.configuration.repository.FieldContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;

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
    private ConfigurationAccessor configurationAccessor;

    @Bean
    @DependsOn({ "descriptorTypeRepository", "registeredDescriptorRepository", "descriptorFieldRepository", "definedFieldRepository", "fieldContextRepository", "configContextRepository" })
    public DescriptorMocker descriptorMocker() {
        return new DescriptorMocker(descriptorTypeRepository, registeredDescriptorRepository, descriptorFieldRepository, definedFieldRepository, fieldContextRepository, configContextRepository);
    }

    public void initBlackDuckData() throws AlertException {
        TestProperties testProperties = new TestProperties();

        ConfigurationFieldModel blackDuckURLField = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_URL);
        blackDuckURLField.setFieldValue(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_URL));

        ConfigurationFieldModel blackDuckAPITokenField = ConfigurationFieldModel.createSensitive(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY);
        blackDuckAPITokenField.setFieldValue(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_API_KEY));

        ConfigurationFieldModel blackDuckTimeoutField = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT);
        blackDuckTimeoutField.setFieldValue(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_TIMEOUT));

        BlackDuckProviderKey blackDuckProviderKey = new BlackDuckProviderKey();
        ConfigurationModel blackDuckConfiguration = configurationAccessor.getConfigurationByDescriptorKeyAndContext(blackDuckProviderKey, ConfigContextEnum.GLOBAL).stream().findFirst()
                                                        .orElse(

                                                            configurationAccessor.createConfiguration(blackDuckProviderKey, ConfigContextEnum.GLOBAL, List.of(blackDuckURLField, blackDuckAPITokenField, blackDuckTimeoutField))

                                                        );
        configurationAccessor.updateConfiguration(blackDuckConfiguration.getConfigurationId(), List.of(blackDuckURLField, blackDuckAPITokenField, blackDuckTimeoutField));
    }

}
