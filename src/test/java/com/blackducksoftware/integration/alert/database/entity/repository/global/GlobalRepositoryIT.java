package com.blackducksoftware.integration.alert.database.entity.repository.global;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.blackducksoftware.integration.alert.Application;
import com.blackducksoftware.integration.alert.database.DatabaseDataSource;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalBlackDuckConfigEntity;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalBlackDuckRepository;
import com.blackducksoftware.integration.test.annotation.DatabaseConnectionTest;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@Category(DatabaseConnectionTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DatabaseDataSource.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class GlobalRepositoryIT {
    @Autowired
    private GlobalBlackDuckRepository repository;

    @Test
    public void testSaveEntity() {
        repository.deleteAll();
        final Integer hubTimeout = 300;
        final String hubApiKey = "hub_api_key";
        final GlobalBlackDuckConfigEntity entity = new GlobalBlackDuckConfigEntity(hubTimeout, hubApiKey);
        final GlobalBlackDuckConfigEntity savedEntity = repository.save(entity);
        final long count = repository.count();
        assertEquals(1, count);
        final Optional<GlobalBlackDuckConfigEntity> foundEntity = repository.findById(savedEntity.getId());
        assertEquals(hubTimeout, foundEntity.get().getBlackDuckTimeout());
        assertEquals(hubApiKey, foundEntity.get().getBlackDuckApiKey());
    }
}
