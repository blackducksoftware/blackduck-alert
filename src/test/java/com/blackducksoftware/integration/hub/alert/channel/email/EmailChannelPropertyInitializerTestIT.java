package com.blackducksoftware.integration.hub.alert.channel.email;

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
import com.blackducksoftware.integration.hub.alert.channel.email.controller.global.GlobalEmailConfigRestModel;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global.GlobalHipChatConfigEntity;
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
public class EmailChannelPropertyInitializerTestIT {

    @Autowired
    private GlobalEmailRepositoryWrapper repository;

    @Test
    public void testChannelNamePrefix() {
        final EmailChannelPropertyInitializer initializer = new EmailChannelPropertyInitializer(repository);
        assertEquals(EmailChannelPropertyInitializer.PROPERTY_PREFIX_CHANNEL_EMAIL, initializer.getPropertyNamePrefix());
    }

    @Test
    public void testEntityClass() {
        final EmailChannelPropertyInitializer initializer = new EmailChannelPropertyInitializer(repository);
        assertEquals(GlobalEmailConfigEntity.class, initializer.getEntityClass());
    }

    @Test
    public void testRestModelInstance() {
        final EmailChannelPropertyInitializer initializer = new EmailChannelPropertyInitializer(repository);
        final ConfigRestModel model = initializer.getRestModelInstance();
        assertNotNull(model);
        assertTrue(model instanceof GlobalEmailConfigRestModel);
    }

    @Test
    public void testSave() {
        repository.deleteAll();
        final EmailChannelPropertyInitializer initializer = new EmailChannelPropertyInitializer(repository);
        final GlobalEmailConfigEntity entity = new GlobalEmailConfigEntity("mailHost", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        initializer.save(entity);
        assertEquals(1, repository.count());
        final GlobalEmailConfigEntity savedEntity = repository.findAll().get(0);
        assertEquals(entity.getMailSmtpHost(), savedEntity.getMailSmtpHost());
    }

    @Test
    public void testSaveWrongEntity() {
        repository.deleteAll();
        final EmailChannelPropertyInitializer initializer = new EmailChannelPropertyInitializer(repository);
        final GlobalHipChatConfigEntity entity = new GlobalHipChatConfigEntity();
        initializer.save(entity);
        assertEquals(0, repository.count());
    }

    @Test
    public void testCanSetDefault() {
        repository.deleteAll();
        final EmailChannelPropertyInitializer initializer = new EmailChannelPropertyInitializer(repository);
        assertEquals(0, repository.count());
        assertTrue(initializer.canSetDefaultProperties());
        final GlobalEmailConfigEntity entity = new GlobalEmailConfigEntity("mailHost", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        initializer.save(entity);
        assertFalse(initializer.canSetDefaultProperties());
    }
}
