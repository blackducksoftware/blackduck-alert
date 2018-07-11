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
package com.blackducksoftware.integration.alert.channel.slack.repository.distribution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

import com.blackducksoftware.integration.alert.Application;
import com.blackducksoftware.integration.alert.channel.slack.model.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.alert.channel.slack.model.SlackDistributionRepository;
import com.blackducksoftware.integration.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.test.annotation.DatabaseConnectionTest;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@Category(DatabaseConnectionTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = "classpath:spring-test.properties")
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class SlackDistributionRepositoryTestIT {
    @Autowired
    private SlackDistributionRepository slackDistributionRepository;

    @Before
    public void cleanup() {
        slackDistributionRepository.deleteAll();
    }

    @Test
    public void saveEntityTestIT() {
        final String channelName = "My Channel";
        final String webhook = "Webhook";
        final String channelUsername = "BlackDuck-alert: test";
        final SlackDistributionConfigEntity entity = new SlackDistributionConfigEntity(webhook, channelUsername, channelName);
        final SlackDistributionConfigEntity savedEntity = slackDistributionRepository.save(entity);
        assertEquals(1, slackDistributionRepository.count());
        assertNotNull(savedEntity.getId());
        assertEquals(channelName, savedEntity.getChannelName());
    }

}
