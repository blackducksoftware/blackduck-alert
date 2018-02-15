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
package com.blackducksoftware.integration.hub.alert.channel.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

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

import com.blackducksoftware.integration.DatabaseConnectionTest;
import com.blackducksoftware.integration.hub.alert.Application;
import com.blackducksoftware.integration.hub.alert.channel.DistributionChannel;
import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.DistributionChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@Category(DatabaseConnectionTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = "classpath:spring-test.properties")
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
@WebAppConfiguration
public class DistributionChannelManagerTestIT<E extends AbstractChannelEvent, G extends GlobalChannelConfigEntity, D extends DistributionChannelConfigEntity, R extends CommonDistributionConfigRestModel> {
    @Autowired
    private List<DistributionChannel<E, G, D>> channelList;
    @Autowired
    private List<DistributionChannelManager<G, D, E, R>> channelManagerList;

    @Test
    public void assertManagerHasBeenCreatedForChannel() {
        // DO NOT DELETE THIS TEST
        // This test exists to ensure that when a new channel is created, its necessary pieces are created as well.
        assertEquals(channelList.size(), channelManagerList.size());
    }

    @Test
    public void getGlobalRepositoryTest() {
        for (final DistributionChannelManager<G, D, E, R> manager : channelManagerList) {
            assertNotNull(manager.getGlobalRepository());
        }
    }

    @Test
    public void getLocalRepositoryTest() {
        for (final DistributionChannelManager<G, D, E, R> manager : channelManagerList) {
            assertNotNull(manager.getLocalRepository());
        }
    }

    @Test
    public void getDatabaseEntityClassTest() {
        for (final DistributionChannelManager<G, D, E, R> manager : channelManagerList) {
            assertNotNull(manager.getDatabaseEntityClass());
        }
    }

}
