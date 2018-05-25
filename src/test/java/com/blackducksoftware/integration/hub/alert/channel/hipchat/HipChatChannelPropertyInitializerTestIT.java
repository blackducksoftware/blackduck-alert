package com.blackducksoftware.integration.hub.alert.channel.hipchat;

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
import com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.global.GlobalHipChatConfigRestModel;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global.GlobalHipChatRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
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
public class HipChatChannelPropertyInitializerTestIT {

    @Autowired
    private GlobalHipChatRepositoryWrapper repository;

    @Test
    public void testChannelNamePrefix() {
        final HipChatChannelPropertyInitializer initializer = new HipChatChannelPropertyInitializer(repository);
        assertEquals(HipChatChannelPropertyInitializer.PROPERTY_PREFIX_CHANNEL_HIPCHAT, initializer.getPropertyNamePrefix());
    }

    @Test
    public void testEntityClass() {
        final HipChatChannelPropertyInitializer initializer = new HipChatChannelPropertyInitializer(repository);
        assertEquals(GlobalHipChatConfigEntity.class, initializer.getEntityClass());
    }

    @Test
    public void testRestModelInstance() {
        final HipChatChannelPropertyInitializer initializer = new HipChatChannelPropertyInitializer(repository);
        final ConfigRestModel model = initializer.getRestModelInstance();
        assertNotNull(model);
        assertTrue(model instanceof GlobalHipChatConfigRestModel);
    }

    @Test
    public void testSave() {
        repository.deleteAll();
        final HipChatChannelPropertyInitializer initializer = new HipChatChannelPropertyInitializer(repository);
        final GlobalHipChatConfigEntity entity = new GlobalHipChatConfigEntity("api", "hostServer");
        initializer.save(entity);
        assertEquals(1, repository.count());
        final GlobalHipChatConfigEntity savedEntity = repository.findAll().get(0);
        assertEquals(entity.getApiKey(), savedEntity.getApiKey());
        assertEquals(entity.getHostServer(), savedEntity.getHostServer());
    }

    @Test
    public void testSaveWrongEntity() {
        repository.deleteAll();
        final HipChatChannelPropertyInitializer initializer = new HipChatChannelPropertyInitializer(repository);
        final GlobalEmailConfigEntity entity = new GlobalEmailConfigEntity();
        initializer.save(entity);
        assertEquals(0, repository.count());
    }

    @Test
    public void testCanSetDefault() {
        repository.deleteAll();
        final HipChatChannelPropertyInitializer initializer = new HipChatChannelPropertyInitializer(repository);
        assertEquals(0, repository.count());
        assertTrue(initializer.canSetDefaultProperties());
        final GlobalHipChatConfigEntity entity = new GlobalHipChatConfigEntity("api", "hostServer");
        initializer.save(entity);
        assertFalse(initializer.canSetDefaultProperties());
    }
}
