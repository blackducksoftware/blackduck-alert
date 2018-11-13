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
package com.synopsys.integration.alert.channel.hipchat.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionRepository;

public class HipChatDistributionRepositoryTestIT extends AlertIntegrationTest {
    @Autowired
    private HipChatDistributionRepository hipChatDistributionRepository;

    @Before
    public void cleanUp() {
        hipChatDistributionRepository.deleteAll();
    }

    @Test
    public void saveEntityTestIT() {
        final Integer roomId = 11111;
        final Boolean notify = Boolean.TRUE;
        final String color = "random";
        final HipChatDistributionConfigEntity entity = new HipChatDistributionConfigEntity(roomId, notify, color);
        final HipChatDistributionConfigEntity savedEntity = hipChatDistributionRepository.save(entity);
        assertEquals(1, hipChatDistributionRepository.count());
        assertNotNull(savedEntity.getId());
        assertEquals(roomId, savedEntity.getRoomId());
        assertEquals(notify, savedEntity.getNotify());
        assertEquals(color, savedEntity.getColor());
    }

}
