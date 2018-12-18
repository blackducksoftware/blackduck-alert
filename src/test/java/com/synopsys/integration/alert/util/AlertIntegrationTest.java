package com.synopsys.integration.alert.util;

import org.junit.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.synopsys.integration.alert.Application;
import com.synopsys.integration.alert.database.DatabaseDataSource;

@Tag(TestTags.DEFAULT_INTEGRATION)
@Tag(TestTags.CUSTOM_DATABASE_CONNECTION)
@RunWith(SpringJUnit4ClassRunner.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { Application.class, DatabaseDataSource.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public abstract class AlertIntegrationTest {
    @Test
    public void deleteMe() {
        // FIXME this test should be removed when we no longer use JUnit 4
        //  i.e. when the annotation `@RunWith(SpringJUnit4ClassRunner.class)` is removed from this class
    }
}
