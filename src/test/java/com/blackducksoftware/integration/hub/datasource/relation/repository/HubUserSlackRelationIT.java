/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.datasource.relation.repository;

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
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserSlackRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.HubUserSlackRepository;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class HubUserSlackRelationIT {
    @Autowired
    private HubUserSlackRepository hubUserSlackRepository;

    @Test
    public void addUserSlackTestIT() {
        final Long userConfigId = new Long(0);
        final Long channeConfiglId = new Long(27);
        final HubUserSlackRelation entity = new HubUserSlackRelation(userConfigId, channeConfiglId);
        final HubUserSlackRelation savedEntity = hubUserSlackRepository.save(entity);

        final long count = hubUserSlackRepository.count();
        assertEquals(1, count);

        final HubUserSlackRelation foundEntity = hubUserSlackRepository.findOne(savedEntity.getUserConfidId());
        assertEquals(channeConfiglId, foundEntity.getChannelConfigId());
    }
}
