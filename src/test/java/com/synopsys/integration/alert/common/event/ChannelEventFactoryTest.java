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
package com.synopsys.integration.alert.common.event;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.junit.Test;
import org.springframework.core.convert.support.DefaultConversionService;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.event.ChannelEvent;
import com.synopsys.integration.alert.channel.event.ChannelEventFactory;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.digest.model.DigestModel;
import com.synopsys.integration.alert.common.digest.model.ProjectData;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.rest.RestConstants;

public class ChannelEventFactoryTest {
    private static final String DISTRIBUTION_TYPE = "TYPE";

    @Test
    public void createEventWithChannelManagerTest() {
        final Gson gson = new Gson();
        final ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());
        final ChannelEventFactory factory = new ChannelEventFactory();

        final Long id = 25L;

        final Collection<ProjectData> projectData = Arrays.asList(new ProjectData(FrequencyType.REAL_TIME, "Project Name", "Project Version", Collections.emptyList(), Collections.emptyMap()));
        final DigestModel digestModel = new DigestModel(projectData);

        final NotificationContent notificationContent = new NotificationContent(new Date(), "provider", "notificationType", contentConverter.getJsonString(digestModel));
        final ChannelEvent expected = new ChannelEvent(DISTRIBUTION_TYPE, RestConstants.formatDate(notificationContent.getCreatedAt()), notificationContent.getProvider(), notificationContent.getNotificationType(),
        notificationContent.getContent(), id, 1L);

        final ChannelEvent event = factory.createChannelEvent(id, "TYPE", notificationContent);
        assertEquals(expected.getAuditEntryId(), event.getAuditEntryId());
        assertEquals(expected.getDestination(), event.getDestination());
        assertEquals(expected.getContent(), event.getContent());
    }

}
