package com.blackducksoftware.integration.hub.datasource.repository;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.blackducksoftware.integration.hub.alert.Application;
import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.hub.alert.datasource.entity.SlackConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.SlackRepository;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class SlackRepositoryIT {

    @Autowired
    private SlackRepository repository;

    @Test
    public void testSaveEntity() {
        final String channelName = "channel_name";
        final String username = "user_name";
        final String webhook = "web_hook";

        final SlackConfigEntity entity = new SlackConfigEntity(channelName, username, webhook);

        final SlackConfigEntity savedEntity = repository.save(entity);
        final long count = repository.count();
        assertEquals(1, count);
        final SlackConfigEntity foundEntity = repository.findOne(savedEntity.getId());
        assertEquals(channelName, foundEntity.getChannelName());
        assertEquals(username, foundEntity.getUsername());
        assertEquals(webhook, foundEntity.getWebhook());
    }
}
