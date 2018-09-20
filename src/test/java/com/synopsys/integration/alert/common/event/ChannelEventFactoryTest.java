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
import static org.junit.Assert.assertSame;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.email.EmailChannelEvent;
import com.synopsys.integration.alert.channel.email.EmailGroupChannel;
import com.synopsys.integration.alert.channel.event.ChannelEventFactory;
import com.synopsys.integration.alert.channel.hipchat.HipChatChannel;
import com.synopsys.integration.alert.channel.hipchat.HipChatChannelEvent;
import com.synopsys.integration.alert.channel.slack.SlackChannel;
import com.synopsys.integration.alert.channel.slack.SlackChannelEvent;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.model.LinkableItem;
import com.synopsys.integration.alert.database.channel.email.EmailDistributionRepositoryAccessor;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionRepositoryAccessor;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.mock.MockBlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.mock.MockBlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.mock.MockUserProjectRelationRepositoryAccessor;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;
import com.synopsys.integration.rest.RestConstants;

public class ChannelEventFactoryTest {
    //TODO add more tests for the events

    @Test
    public void createEmailEvent() throws Exception {
        final Gson gson = new Gson();
        final EmailDistributionRepositoryAccessor emailDistributionRepositoryAccessor = Mockito.mock(EmailDistributionRepositoryAccessor.class);
        final HipChatDistributionRepositoryAccessor hipChatDistributionRepositoryAccessor = Mockito.mock(HipChatDistributionRepositoryAccessor.class);
        final SlackDistributionRepositoryAccessor slackDistributionRepositoryAccessor = Mockito.mock(SlackDistributionRepositoryAccessor.class);

        final Long commonDistributionConfigId = 25L;
        final Long distributionConfigId = 33L;
        final String distributionType = EmailGroupChannel.COMPONENT_NAME;
        final String providerName = "provider";

        final CommonDistributionConfig jobConfig = Mockito.mock(CommonDistributionConfig.class);
        Mockito.when(jobConfig.getDistributionType()).thenReturn(distributionType);
        Mockito.when(jobConfig.getDistributionConfigId()).thenReturn("33");
        Mockito.when(jobConfig.getId()).thenReturn("25");
        Mockito.when(jobConfig.getProviderName()).thenReturn(providerName);

        String subjectLine = "Alert unit test";
        EmailGroupDistributionConfigEntity emailEntity = new EmailGroupDistributionConfigEntity(null, subjectLine);

        final Optional optionalDatabaseEntity = Optional.of(emailEntity);
        Mockito.when(emailDistributionRepositoryAccessor.readEntity(ArgumentMatchers.same(distributionConfigId))).thenReturn(optionalDatabaseEntity);

        final MockBlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor = new MockBlackDuckProjectRepositoryAccessor();
        final MockBlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor = new MockBlackDuckUserRepositoryAccessor();
        final MockUserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor = new MockUserProjectRelationRepositoryAccessor();

        final ChannelEventFactory factory = new ChannelEventFactory(emailDistributionRepositoryAccessor, hipChatDistributionRepositoryAccessor, slackDistributionRepositoryAccessor,
            blackDuckProjectRepositoryAccessor, blackDuckUserRepositoryAccessor, userProjectRelationRepositoryAccessor, gson);

        final LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        final AggregateMessageContent content = new AggregateMessageContent("testTopic", "", null, subTopic, Collections.emptyList());

        final EmailChannelEvent expected = new EmailChannelEvent(RestConstants.formatDate(new Date()), providerName,
            content, commonDistributionConfigId, Collections.emptySet(), subjectLine);

        final EmailChannelEvent event = (EmailChannelEvent) factory.createChannelEvent(jobConfig, content);
        assertEquals(expected.getAuditEntryId(), event.getAuditEntryId());
        assertEquals(expected.getDestination(), event.getDestination());
        assertEquals(expected.getProvider(), event.getProvider());
        assertEquals(expected.getContent(), event.getContent());
        assertEquals(expected.getSubjectLine(), event.getSubjectLine());
    }

    @Test
    public void createHipChatEvent() throws Exception {
        final Gson gson = new Gson();
        final EmailDistributionRepositoryAccessor emailDistributionRepositoryAccessor = Mockito.mock(EmailDistributionRepositoryAccessor.class);
        final HipChatDistributionRepositoryAccessor hipChatDistributionRepositoryAccessor = Mockito.mock(HipChatDistributionRepositoryAccessor.class);
        final SlackDistributionRepositoryAccessor slackDistributionRepositoryAccessor = Mockito.mock(SlackDistributionRepositoryAccessor.class);

        final Long commonDistributionConfigId = 25L;
        final Long distributionConfigId = 33L;
        final String distributionType = HipChatChannel.COMPONENT_NAME;
        final String providerName = "provider";

        final CommonDistributionConfig jobConfig = Mockito.mock(CommonDistributionConfig.class);
        Mockito.when(jobConfig.getDistributionType()).thenReturn(distributionType);
        Mockito.when(jobConfig.getDistributionConfigId()).thenReturn("33");
        Mockito.when(jobConfig.getId()).thenReturn("25");
        Mockito.when(jobConfig.getProviderName()).thenReturn(providerName);

        Integer roomId = 100484;
        Boolean notify = false;
        String color = "red";
        HipChatDistributionConfigEntity hipChatEntity = new HipChatDistributionConfigEntity(roomId, notify, color);

        final Optional optionalDatabaseEntity = Optional.of(hipChatEntity);
        Mockito.when(hipChatDistributionRepositoryAccessor.readEntity(ArgumentMatchers.same(distributionConfigId))).thenReturn(optionalDatabaseEntity);

        final MockBlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor = new MockBlackDuckProjectRepositoryAccessor();
        final MockBlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor = new MockBlackDuckUserRepositoryAccessor();
        final MockUserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor = new MockUserProjectRelationRepositoryAccessor();

        final ChannelEventFactory factory = new ChannelEventFactory(emailDistributionRepositoryAccessor, hipChatDistributionRepositoryAccessor, slackDistributionRepositoryAccessor,
            blackDuckProjectRepositoryAccessor, blackDuckUserRepositoryAccessor, userProjectRelationRepositoryAccessor, gson);

        final LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        final AggregateMessageContent content = new AggregateMessageContent("testTopic", "", null, subTopic, Collections.emptyList());

        final HipChatChannelEvent expected = new HipChatChannelEvent(RestConstants.formatDate(new Date()), providerName,
            content, commonDistributionConfigId, roomId, notify, color);

        final HipChatChannelEvent event = (HipChatChannelEvent) factory.createChannelEvent(jobConfig, content);
        assertEquals(expected.getAuditEntryId(), event.getAuditEntryId());
        assertEquals(expected.getDestination(), event.getDestination());
        assertEquals(expected.getProvider(), event.getProvider());
        assertEquals(expected.getContent(), event.getContent());
        assertSame(expected.getRoomId(), event.getRoomId());
        assertEquals(expected.getNotify(), event.getNotify());
        assertEquals(expected.getColor(), event.getColor());
    }

    @Test
    public void createSlackEvent() throws Exception {
        final Gson gson = new Gson();
        final EmailDistributionRepositoryAccessor emailDistributionRepositoryAccessor = Mockito.mock(EmailDistributionRepositoryAccessor.class);
        final HipChatDistributionRepositoryAccessor hipChatDistributionRepositoryAccessor = Mockito.mock(HipChatDistributionRepositoryAccessor.class);
        final SlackDistributionRepositoryAccessor slackDistributionRepositoryAccessor = Mockito.mock(SlackDistributionRepositoryAccessor.class);

        final Long commonDistributionConfigId = 25L;
        final Long distributionConfigId = 33L;
        final String distributionType = SlackChannel.COMPONENT_NAME;
        final String providerName = "provider";

        final CommonDistributionConfig jobConfig = Mockito.mock(CommonDistributionConfig.class);
        Mockito.when(jobConfig.getDistributionType()).thenReturn(distributionType);
        Mockito.when(jobConfig.getDistributionConfigId()).thenReturn("33");
        Mockito.when(jobConfig.getId()).thenReturn("25");
        Mockito.when(jobConfig.getProviderName()).thenReturn(providerName);

        String channelUsername = "Slack UserName";
        String webhook = "WebHook";
        String channelName = "Alert Channel";
        SlackDistributionConfigEntity slackEntity = new SlackDistributionConfigEntity(webhook, channelUsername, channelName);

        final Optional optionalDatabaseEntity = Optional.of(slackEntity);
        Mockito.when(slackDistributionRepositoryAccessor.readEntity(ArgumentMatchers.same(distributionConfigId))).thenReturn(optionalDatabaseEntity);

        final MockBlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor = new MockBlackDuckProjectRepositoryAccessor();
        final MockBlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor = new MockBlackDuckUserRepositoryAccessor();
        final MockUserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor = new MockUserProjectRelationRepositoryAccessor();

        final ChannelEventFactory factory = new ChannelEventFactory(emailDistributionRepositoryAccessor, hipChatDistributionRepositoryAccessor, slackDistributionRepositoryAccessor,
            blackDuckProjectRepositoryAccessor, blackDuckUserRepositoryAccessor, userProjectRelationRepositoryAccessor, gson);

        final LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        final AggregateMessageContent content = new AggregateMessageContent("testTopic", "", null, subTopic, Collections.emptyList());

        final SlackChannelEvent expected = new SlackChannelEvent(RestConstants.formatDate(new Date()), providerName,
            content, commonDistributionConfigId, channelUsername, webhook, channelName);

        final SlackChannelEvent event = (SlackChannelEvent) factory.createChannelEvent(jobConfig, content);
        assertEquals(expected.getAuditEntryId(), event.getAuditEntryId());
        assertEquals(expected.getDestination(), event.getDestination());
        assertEquals(expected.getProvider(), event.getProvider());
        assertEquals(expected.getContent(), event.getContent());
        assertEquals(expected.getChannelUsername(), event.getChannelUsername());
        assertEquals(expected.getWebHook(), event.getWebHook());
        assertEquals(expected.getChannelName(), event.getChannelName());
    }

}
