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

import org.junit.Test;
import org.springframework.core.convert.support.DefaultConversionService;

import com.blackducksoftware.integration.alert.channel.event.ChannelEvent;
import com.blackducksoftware.integration.alert.channel.event.ChannelEventFactory;
import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.digest.model.DigestModel;
import com.blackducksoftware.integration.alert.common.digest.model.ProjectData;
import com.blackducksoftware.integration.alert.common.enumeration.DigestType;
import com.google.gson.Gson;

public class ChannelEventFactoryTest {
    private static final String DISTRIBUTION_TYPE = "TYPE";

    @Test
    public void createEventWithChannelManagerTest() {
        final Gson gson = new Gson();
        final ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());
        final ChannelEventFactory factory = new ChannelEventFactory(contentConverter);

        final Long id = 25L;
        final Collection<ProjectData> projectData = Arrays.asList(new ProjectData(DigestType.REAL_TIME, "Project Name", "Project Version", Collections.emptyList(), Collections.emptyMap()));
        final DigestModel digestModel = new DigestModel(projectData);
        final ChannelEvent expected = new ChannelEvent(DISTRIBUTION_TYPE, contentConverter.getJsonString(digestModel), id);

        final ChannelEvent event = factory.createChannelEvent("TYPE", digestModel, id);
        assertEquals(expected.getAuditEntryId(), event.getAuditEntryId());
        assertEquals(expected.getDestination(), event.getDestination());
        assertEquals(expected.getContent(), event.getContent());
    }

}
