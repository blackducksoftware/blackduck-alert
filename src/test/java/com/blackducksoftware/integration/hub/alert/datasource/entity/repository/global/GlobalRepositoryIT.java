package com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global;

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
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHubConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHubRepository;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class GlobalRepositoryIT {

    @Autowired
    private GlobalHubRepository repository;

    @Test
    public void testSaveEntity() {
        final Integer hubTimeout = 300;
        final String hubUsername = "hub_username";
        final String hubPassword = "hub_password";
        final String accumulatorCron = "accumulator_cron";
        final String dailyDigestCron = "dailyDigest_cron";
        final String purgeDataCron = "purgeData_cron";
        final GlobalHubConfigEntity entity = new GlobalHubConfigEntity(hubTimeout, hubUsername, hubPassword, accumulatorCron, dailyDigestCron, purgeDataCron);
        final GlobalHubConfigEntity savedEntity = repository.save(entity);
        final long count = repository.count();
        assertEquals(1, count);
        final GlobalHubConfigEntity foundEntity = repository.findOne(savedEntity.getId());
        assertEquals(hubTimeout, foundEntity.getHubTimeout());
        assertEquals(hubUsername, foundEntity.getHubUsername());
        assertEquals(hubPassword, foundEntity.getHubPassword());
        assertEquals(accumulatorCron, foundEntity.getAccumulatorCron());
        assertEquals(dailyDigestCron, foundEntity.getDailyDigestCron());
        assertEquals(purgeDataCron, foundEntity.getPurgeDataCron());
    }
}
