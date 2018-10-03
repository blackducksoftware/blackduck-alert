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
package com.synopsys.integration.alert.channel;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.junit.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.TestAlertProperties;
import com.synopsys.integration.alert.TestBlackDuckProperties;
import com.synopsys.integration.alert.channel.email.EmailChannelEvent;
import com.synopsys.integration.alert.channel.email.EmailGroupChannel;
import com.synopsys.integration.alert.channel.email.mock.MockEmailGlobalEntity;
import com.synopsys.integration.alert.channel.slack.SlackChannel;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.model.LinkableItem;
import com.synopsys.integration.alert.database.api.AuditUtility;
import com.synopsys.integration.alert.database.channel.email.EmailGlobalConfigEntity;
import com.synopsys.integration.alert.database.channel.email.EmailGlobalRepository;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;

public class DistributionChannelTest extends ChannelTest {

    @Test
    public void getGlobalConfigEntityTest() {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final BlackDuckProperties hubProperties = new TestBlackDuckProperties(testAlertProperties);
        final EmailGlobalRepository emailGlobalRepository = Mockito.mock(EmailGlobalRepository.class);
        final EmailGroupChannel channel = new EmailGroupChannel(gson, testAlertProperties, hubProperties, null, emailGlobalRepository);

        final MockEmailGlobalEntity mockEntity = new MockEmailGlobalEntity();
        final EmailGlobalConfigEntity entity = mockEntity.createGlobalEntity();
        Mockito.when(emailGlobalRepository.findAll()).thenReturn(Arrays.asList(entity));

        final EmailGlobalConfigEntity globalEntity = channel.getGlobalConfigEntity();
        assertEquals(entity, globalEntity);
    }

    @Test
    public void receiveMessageTest() {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final BlackDuckProperties hubProperties = new TestBlackDuckProperties(testAlertProperties);
        final AuditUtility auditUtility = Mockito.mock(AuditUtility.class);
        final EmailGlobalRepository emailGlobalRepository = Mockito.mock(EmailGlobalRepository.class);

        final EmailGroupChannel channel = new EmailGroupChannel(gson, testAlertProperties, hubProperties, auditUtility, emailGlobalRepository);
        final LinkableItem subTopic = new LinkableItem("subTopic", "sub topic", null);
        final AggregateMessageContent content = new AggregateMessageContent("testTopic", "Distribution Channel Test", null, subTopic, Collections.emptyList());

        final EmailChannelEvent event = new EmailChannelEvent(RestConstants.formatDate(new Date()), "provider", "FORMAT",
            content, 1L, Collections.emptySet(), "TEST SUBJECT LINE");

        channel.handleEvent(event);
    }

    @Test
    public void testGlobalConfigTest() throws IntegrationException {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final BlackDuckProperties hubProperties = new TestBlackDuckProperties(testAlertProperties);
        // Slack has no global config, so we use it to test the default method.
        final SlackChannel slackChannel = new SlackChannel(gson, testAlertProperties, hubProperties, null, null);
        final Config globalConfig = Mockito.mock(Config.class);

        final String nullMessage = slackChannel.testGlobalConfig(null);
        assertEquals("The provided config was null.", nullMessage);
        try {
            slackChannel.testGlobalConfig(globalConfig);
        } catch (final AlertException ex) {
            assertEquals("Test method not implemented.", ex.getMessage());
        }
    }

}
