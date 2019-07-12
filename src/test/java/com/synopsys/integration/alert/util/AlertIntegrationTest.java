package com.synopsys.integration.alert.util;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.synopsys.integration.alert.Application;
import com.synopsys.integration.alert.ApplicationConfiguration;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.database.DatabaseDataSource;
import com.synopsys.integration.alert.database.configuration.repository.ConfigContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.DefinedFieldRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorFieldRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorTypeRepository;
import com.synopsys.integration.alert.database.configuration.repository.FieldContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;

@Tag(TestTags.DEFAULT_INTEGRATION)
@Tag(TestTags.CUSTOM_DATABASE_CONNECTION)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { Application.class, ApplicationConfiguration.class, DatabaseDataSource.class, DescriptorMocker.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@WebAppConfiguration
@Transactional
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public abstract class AlertIntegrationTest {
    public static final String ROLE_ALERT_ADMIN = "ALERT_ADMIN";

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

        final ConfigurationModel blackDuckConfiguration = configurationAccessor.getConfigurationByDescriptorNameAndContext(BlackDuckProvider.COMPONENT_NAME, ConfigContextEnum.GLOBAL).stream().findFirst()
                                                              .orElse(

                                                                  configurationAccessor
                                                                      .createConfiguration(BlackDuckProvider.COMPONENT_NAME, ConfigContextEnum.GLOBAL, List.of(blackDuckURLField, blackDuckAPITokenField, blackDuckTimeoutField))

                                                              );
        configurationAccessor.updateConfiguration(blackDuckConfiguration.getConfigurationId(), List.of(blackDuckURLField, blackDuckAPITokenField, blackDuckTimeoutField));
    }
}
