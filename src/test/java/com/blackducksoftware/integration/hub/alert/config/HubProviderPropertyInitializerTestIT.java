package com.blackducksoftware.integration.hub.alert.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

import com.blackducksoftware.integration.hub.alert.Application;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHubConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHubRepository;
import com.blackducksoftware.integration.hub.alert.hub.controller.global.GlobalHubConfigRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;
import com.blackducksoftware.integration.test.annotation.DatabaseConnectionTest;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@Category(DatabaseConnectionTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = "classpath:spring-test.properties")
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class HubProviderPropertyInitializerTestIT {

    @Autowired
    private GlobalHubRepository repository;

    @Test
    public void testChannelNamePrefix() {
        final HubProviderPropertyInitializer initializer = new HubProviderPropertyInitializer(repository);
        assertEquals(HubProviderPropertyInitializer.PROPERTY_PREFIX_PROVIDER_HUB, initializer.getPropertyNamePrefix());
    }

    @Test
    public void testEntityClass() {
        final HubProviderPropertyInitializer initializer = new HubProviderPropertyInitializer(repository);
        assertEquals(GlobalHubConfigEntity.class, initializer.getEntityClass());
    }

    @Test
    public void testRestModelInstance() {
        final HubProviderPropertyInitializer initializer = new HubProviderPropertyInitializer(repository);
        final ConfigRestModel model = initializer.getRestModelInstance();
        assertNotNull(model);
        assertTrue(model instanceof GlobalHubConfigRestModel);
    }

    @Test
    public void testSave() {
        repository.deleteAll();
        final HubProviderPropertyInitializer initializer = new HubProviderPropertyInitializer(repository);
        final GlobalHubConfigEntity entity = new GlobalHubConfigEntity(300, "apiKey");
        initializer.save(entity);
        assertEquals(1, repository.count());
        final GlobalHubConfigEntity savedEntity = repository.findAll().get(0);
        assertEquals(entity.getHubTimeout(), savedEntity.getHubTimeout());
        assertEquals(entity.getHubApiKey(), savedEntity.getHubApiKey());
    }

    @Test
    public void testSaveWrongEntity() {
        repository.deleteAll();
        final HubProviderPropertyInitializer initializer = new HubProviderPropertyInitializer(repository);
        final GlobalEmailConfigEntity entity = new GlobalEmailConfigEntity();
        initializer.save(entity);
        assertEquals(0, repository.count());
    }

    @Test
    public void testSavePreexistingEntitySaveDefault() {
        repository.deleteAll();
        final HubProviderPropertyInitializer initializer = new HubProviderPropertyInitializer(repository);
        final GlobalHubConfigEntity entity = new GlobalHubConfigEntity(300, "apiKey");
        final GlobalHubConfigEntity entityToSave = new GlobalHubConfigEntity(600, "defaultApiKey");
        repository.save(entity);
        initializer.save(entityToSave);
        assertEquals(1, repository.count());
        final GlobalHubConfigEntity savedEntity = repository.findAll().get(0);
        assertEquals(entity.getHubTimeout(), savedEntity.getHubTimeout());
        assertEquals(entity.getHubApiKey(), savedEntity.getHubApiKey());
    }

    @Test
    public void testSavePreexistingValuesNotOverwritten() {
        repository.deleteAll();
        final HubProviderPropertyInitializer initializer = new HubProviderPropertyInitializer(repository);
        final GlobalHubConfigEntity entity = new GlobalHubConfigEntity(null, null);
        final GlobalHubConfigEntity entityToSave = new GlobalHubConfigEntity(300, "apiKeyDefault");
        repository.save(entity);
        initializer.save(entityToSave);
        assertEquals(1, repository.count());
        final GlobalHubConfigEntity savedEntity = repository.findAll().get(0);
        assertEquals(entity.getHubTimeout(), savedEntity.getHubTimeout());
        assertEquals(entityToSave.getHubApiKey(), savedEntity.getHubApiKey());
    }

    @Test
    public void testCanSetDefault() {
        repository.deleteAll();
        final HubProviderPropertyInitializer initializer = new HubProviderPropertyInitializer(repository);
        assertEquals(0, repository.count());
        assertTrue(initializer.canSetDefaultProperties());
        final GlobalHubConfigEntity entity = new GlobalHubConfigEntity(300, "apiKey");
        initializer.save(entity);
        assertFalse(initializer.canSetDefaultProperties());
    }

}
