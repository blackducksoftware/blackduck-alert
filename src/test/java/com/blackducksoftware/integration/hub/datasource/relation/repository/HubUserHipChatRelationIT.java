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
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserHipChatRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.HubUserHipChatRepository;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class HubUserHipChatRelationIT {
    @Autowired
    private HubUserHipChatRepository hubUserHipChatRepository;

    @After
    public void cleanup() {
        hubUserHipChatRepository.deleteAll();
    }

    @Test
    public void addUserHipChatTestIT() {
        final Long userConfigId = new Long(0);
        final Long channeConfiglId = new Long(27);
        final HubUserHipChatRelation entity = new HubUserHipChatRelation(userConfigId, channeConfiglId);
        final HubUserHipChatRelation savedEntity = hubUserHipChatRepository.save(entity);

        final long count = hubUserHipChatRepository.count();
        assertEquals(1, count);

        final HubUserHipChatRelation foundEntity = hubUserHipChatRepository.findOne(savedEntity.getUserConfigId());
        assertEquals(channeConfiglId, foundEntity.getChannelConfigId());
    }
}
