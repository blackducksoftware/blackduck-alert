package com.synopsys.integration.alert.database.security.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
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

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.synopsys.integration.alert.Application;
import com.synopsys.integration.alert.database.DatabaseDataSource;
import com.synopsys.integration.alert.database.security.SaltMappingEntity;
import com.synopsys.integration.test.annotation.DatabaseConnectionTest;

@Category(DatabaseConnectionTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DatabaseDataSource.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class SaltMappingRepositoryTestIT {
    @Autowired
    private SaltMappingRepository repository;

    @Before
    public void initTest() {
        final List<SaltMappingEntity> entityList = repository.findAll();
        repository.deleteAll(entityList);
    }

    @Test
    public void testSaveEntity() {
        final String propertyKey = "propertyKey";
        final String salt = "propertySalt";
        final SaltMappingEntity entity = new SaltMappingEntity(propertyKey, salt);
        repository.save(entity);

        final List<SaltMappingEntity> entityList = repository.findAll();
        assertEquals(1, entityList.size());
        final SaltMappingEntity foundEntity = entityList.get(0);
        assertEquals(entity, foundEntity);
    }

    @Test
    public void testFindEntity() {
        final String propertyKey = "expectedPropertyKey";
        final String salt = "expectedPropertySalt";
        final SaltMappingEntity expectedEntity = new SaltMappingEntity(propertyKey, salt);

        for (int index = 0; index < 5; index++) {
            final SaltMappingEntity entity = new SaltMappingEntity("prop_key_" + index, "prop_salt_" + index);
            repository.save(entity);
        }
        repository.save(expectedEntity);
        for (int index = 5; index < 10; index++) {
            final SaltMappingEntity entity = new SaltMappingEntity("prop_key_" + index, "prop_salt_" + index);
            repository.save(entity);
        }
        final Optional<SaltMappingEntity> foundEntity = repository.findById(propertyKey);
        assertTrue(foundEntity.isPresent());
        assertEquals(expectedEntity, foundEntity.get());
    }
}
