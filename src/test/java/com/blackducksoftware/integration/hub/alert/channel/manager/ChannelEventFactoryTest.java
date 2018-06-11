/*
 * Copyright (C) 2018 Black Duck Software Inc.
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
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.digest.model.DigestModel;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.enumeration.DigestTypeEnum;
import com.blackducksoftware.integration.hub.alert.event.AlertEventContentConverter;
import com.blackducksoftware.integration.hub.alert.event.ChannelEvent;
import com.google.gson.Gson;

public class ChannelEventFactoryTest {
    private static final String DISTRIBUTION_TYPE = "TYPE";

    @Test
    public void createEventWithNoChannelManagersTest() {
        final ChannelEventFactory factory = new ChannelEventFactory(null);
        assertNull(factory.createEvent(1L, DISTRIBUTION_TYPE, null));
    }

    @Test
    public void createEventWithChannelManagerTest() {
        final Gson gson = new Gson();
        final AlertEventContentConverter contentConverter = new AlertEventContentConverter(gson);
        final DistributionChannelManager manager = Mockito.mock(DistributionChannelManager.class);
        final ChannelEventFactory factory = new ChannelEventFactory(manager);

        final Long id = 25L;
        final Collection<ProjectData> projectData = Arrays.asList(new ProjectData(DigestTypeEnum.REAL_TIME, "Project Name", "Project Version", Collections.emptyList(), Collections.emptyMap()));
        final DigestModel digestModel = new DigestModel(projectData);
        final ChannelEvent mockEvent = new ChannelEvent(DISTRIBUTION_TYPE, contentConverter.convertToString(digestModel), id);
        Mockito.when(manager.createChannelEvent(Mockito.any(), Mockito.any(), Mockito.anyLong())).thenReturn(mockEvent);

        final ChannelEvent event = factory.createEvent(id, "TYPE", digestModel);
        assertEquals(mockEvent, event);
    }

}
