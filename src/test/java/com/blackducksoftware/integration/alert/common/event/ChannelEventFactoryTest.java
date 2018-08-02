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
package com.blackducksoftware.integration.alert.common.event;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;

import com.blackducksoftware.integration.alert.channel.DistributionChannelManager;
import com.blackducksoftware.integration.alert.channel.event.ChannelEvent;
import com.blackducksoftware.integration.alert.channel.event.ChannelEventFactory;
import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.digest.model.DigestModel;
import com.blackducksoftware.integration.alert.common.digest.model.ProjectData;
import com.blackducksoftware.integration.alert.common.enumeration.DigestType;
import com.blackducksoftware.integration.alert.database.entity.NotificationContent;
import com.blackducksoftware.integration.rest.connection.RestConnection;
import com.google.gson.Gson;

public class ChannelEventFactoryTest {
    private static final String DISTRIBUTION_TYPE = "TYPE";

    @Test
    public void createEventWithChannelManagerTest() {
        final Gson gson = new Gson();
        final ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());
        final DistributionChannelManager manager = Mockito.mock(DistributionChannelManager.class);
        final ChannelEventFactory factory = new ChannelEventFactory(manager);

        final Long id = 25L;

        final Collection<ProjectData> projectData = Arrays.asList(new ProjectData(DigestType.REAL_TIME, "Project Name", "Project Version", Collections.emptyList(), Collections.emptyMap()));
        final DigestModel digestModel = new DigestModel(projectData);

        final NotificationContent notificationContent = new NotificationContent(new Date(), "provider", "notificationType", contentConverter.getJsonString(digestModel));
        final ChannelEvent mockEvent = new ChannelEvent(DISTRIBUTION_TYPE, RestConnection.formatDate(notificationContent.getCreatedAt()), notificationContent.getProvider(), notificationContent.getNotificationType(),
                notificationContent.getContent(), id, 1L);
        Mockito.when(manager.createChannelEvent(Mockito.any(), Mockito.any(), Mockito.anyLong())).thenReturn(mockEvent);

        final ChannelEvent event = factory.createEvent(id, "TYPE", notificationContent);
        assertEquals(mockEvent, event);
    }

}
