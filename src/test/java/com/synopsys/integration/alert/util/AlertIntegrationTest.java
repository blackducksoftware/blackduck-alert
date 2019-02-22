package com.synopsys.integration.alert.util;

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
import com.synopsys.integration.alert.database.DatabaseDataSource;
import com.synopsys.integration.alert.database.configuration.repository.ConfigContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.DefinedFieldRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorFieldRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorTypeRepository;
import com.synopsys.integration.alert.database.configuration.repository.FieldContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;

@Tag(TestTags.DEFAULT_INTEGRATION)
@Tag(TestTags.CUSTOM_DATABASE_CONNECTION)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { Application.class, DatabaseDataSource.class, DescriptorMocker.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@WebAppConfiguration
@Transactional
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public abstract class AlertIntegrationTest {
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

    @Bean
    @DependsOn({ "descriptorTypeRepository", "registeredDescriptorRepository", "descriptorFieldRepository", "definedFieldRepository", "fieldContextRepository", "configContextRepository" })
    public DescriptorMocker descriptorMocker() {
        return new DescriptorMocker(descriptorTypeRepository, registeredDescriptorRepository, descriptorFieldRepository, definedFieldRepository, fieldContextRepository, configContextRepository);
    }

}
