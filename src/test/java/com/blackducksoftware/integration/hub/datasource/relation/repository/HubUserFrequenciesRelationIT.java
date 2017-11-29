package com.blackducksoftware.integration.hub.datasource.relation.repository;

import static org.junit.Assert.assertEquals;

import org.junit.After;
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
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserFrequenciesRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.HubUserFrequenciesRepository;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class HubUserFrequenciesRelationIT {
    @Autowired
    private HubUserFrequenciesRepository hubUserFrequenciesRepository;

    @After
    public void cleanup() {
        hubUserFrequenciesRepository.deleteAll();
    }

    @Test
    public void addUserFrequencyTestIT() {
        final Long userConfigId = new Long(0);
        final String frequency = "REAL_TIME";
        final HubUserFrequenciesRelation entity = new HubUserFrequenciesRelation(userConfigId, frequency);
        final HubUserFrequenciesRelation savedEntity = hubUserFrequenciesRepository.save(entity);

        final long count = hubUserFrequenciesRepository.count();
        assertEquals(1, count);

        final HubUserFrequenciesRelation foundEntity = hubUserFrequenciesRepository.findOne(savedEntity.getUserConfigId());
        assertEquals(frequency, foundEntity.getFrequency());
    }
}
