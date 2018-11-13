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
package com.synopsys.integration.alert.channel.slack.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionRepository;

public class SlackDistributionRepositoryTestIT extends AlertIntegrationTest {
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
