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
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

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
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelation;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.mock.MockBlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.mock.MockBlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.mock.MockUserProjectRelationRepositoryAccessor;
import com.synopsys.integration.alert.web.channel.model.EmailDistributionConfig;
import com.synopsys.integration.alert.web.channel.model.HipChatDistributionConfig;
import com.synopsys.integration.alert.web.channel.model.SlackDistributionConfig;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;
import com.synopsys.integration.rest.RestConstants;

public class ChannelEventFactoryTest {
    //TODO add more tests for the events

    @Test
    public void createEmailEvent() throws Exception {
        final EmailDistributionRepositoryAccessor emailDistributionRepositoryAccessor = Mockito.mock(EmailDistributionRepositoryAccessor.class);
        final HipChatDistributionRepositoryAccessor hipChatDistributionRepositoryAccessor = Mockito.mock(HipChatDistributionRepositoryAccessor.class);
        final SlackDistributionRepositoryAccessor slackDistributionRepositoryAccessor = Mockito.mock(SlackDistributionRepositoryAccessor.class);

        final Long commonDistributionConfigId = 25L;
        final Long distributionConfigId = 33L;
        final String distributionType = EmailGroupChannel.COMPONENT_NAME;
        final String providerName = BlackDuckProvider.COMPONENT_NAME;

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
            blackDuckProjectRepositoryAccessor, blackDuckUserRepositoryAccessor, userProjectRelationRepositoryAccessor);

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
        assertEquals(expected.getEmailAddresses(), event.getEmailAddresses());
    }

    @Test
    public void createEmailEventWithEmailAddresses() throws Exception {
        final EmailDistributionRepositoryAccessor emailDistributionRepositoryAccessor = Mockito.mock(EmailDistributionRepositoryAccessor.class);
        final HipChatDistributionRepositoryAccessor hipChatDistributionRepositoryAccessor = Mockito.mock(HipChatDistributionRepositoryAccessor.class);
        final SlackDistributionRepositoryAccessor slackDistributionRepositoryAccessor = Mockito.mock(SlackDistributionRepositoryAccessor.class);

        final Long commonDistributionConfigId = 25L;
        final Long distributionConfigId = 33L;
        final String distributionType = EmailGroupChannel.COMPONENT_NAME;
        final String providerName = BlackDuckProvider.COMPONENT_NAME;

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

        DatabaseEntity projectEntity1 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity("Project one", "", ""));

        String email1 = "Test Email 1";
        String email2 = "Test Email 2";
        DatabaseEntity userEntity1 = blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity(email1, false));
        DatabaseEntity userEntity2 = blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity(email2, false));

        UserProjectRelation userProjectRelation1 = new UserProjectRelation(userEntity1.getId(), projectEntity1.getId());
        UserProjectRelation userProjectRelation2 = new UserProjectRelation(userEntity2.getId(), projectEntity1.getId());

        userProjectRelationRepositoryAccessor.deleteAndSaveAll(new HashSet<>(Arrays.asList(userProjectRelation1, userProjectRelation2)));

        final ChannelEventFactory factory = new ChannelEventFactory(emailDistributionRepositoryAccessor, hipChatDistributionRepositoryAccessor, slackDistributionRepositoryAccessor,
            blackDuckProjectRepositoryAccessor, blackDuckUserRepositoryAccessor, userProjectRelationRepositoryAccessor);

        final LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        final AggregateMessageContent content = new AggregateMessageContent("testTopic", "Project one", null, subTopic, Collections.emptyList());

        final EmailChannelEvent expected = new EmailChannelEvent(RestConstants.formatDate(new Date()), providerName,
            content, commonDistributionConfigId, new HashSet<>(Arrays.asList(email1, email2)), subjectLine);

        final EmailChannelEvent event = (EmailChannelEvent) factory.createChannelEvent(jobConfig, content);
        assertEquals(expected.getAuditEntryId(), event.getAuditEntryId());
        assertEquals(expected.getDestination(), event.getDestination());
        assertEquals(expected.getProvider(), event.getProvider());
        assertEquals(expected.getContent(), event.getContent());
        assertEquals(expected.getSubjectLine(), event.getSubjectLine());
        assertEquals(expected.getEmailAddresses(), event.getEmailAddresses());
    }

    @Test
    public void createTestEmailEvent() throws Exception {
        final EmailDistributionRepositoryAccessor emailDistributionRepositoryAccessor = Mockito.mock(EmailDistributionRepositoryAccessor.class);
        final HipChatDistributionRepositoryAccessor hipChatDistributionRepositoryAccessor = Mockito.mock(HipChatDistributionRepositoryAccessor.class);
        final SlackDistributionRepositoryAccessor slackDistributionRepositoryAccessor = Mockito.mock(SlackDistributionRepositoryAccessor.class);

        final Long commonDistributionConfigId = 25L;
        final Long distributionConfigId = 33L;
        final String distributionType = EmailGroupChannel.COMPONENT_NAME;
        final String providerName = BlackDuckProvider.COMPONENT_NAME;

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
            blackDuckProjectRepositoryAccessor, blackDuckUserRepositoryAccessor, userProjectRelationRepositoryAccessor);

        AggregateMessageContent testContent = createTestNotificationContent();

        final EmailChannelEvent expectedTest = new EmailChannelEvent(RestConstants.formatDate(new Date()), providerName,
            testContent, commonDistributionConfigId, Collections.emptySet(), subjectLine);

        EmailDistributionConfig emailDistributionConfig = new EmailDistributionConfig("1", "1", distributionType, "Test Email Job", BlackDuckProvider.COMPONENT_NAME, "REAL_TIME", "false",
            "", subjectLine, Collections.emptyList(), Collections.emptyList(), "DEFAULT");

        final EmailChannelEvent testEvent = factory.createEmailChannelTestEvent(emailDistributionConfig);
        assertEquals(expectedTest.getAuditEntryId(), testEvent.getAuditEntryId());
        assertEquals(expectedTest.getDestination(), testEvent.getDestination());
        assertEquals(expectedTest.getProvider(), testEvent.getProvider());
        assertEquals(expectedTest.getContent(), testEvent.getContent());
        assertEquals(expectedTest.getSubjectLine(), testEvent.getSubjectLine());
        assertEquals(expectedTest.getEmailAddresses(), testEvent.getEmailAddresses());

    }

    @Test
    public void createTestEmailEventWithEmailAddressesIncludeAllProjects() throws Exception {
        final EmailDistributionRepositoryAccessor emailDistributionRepositoryAccessor = Mockito.mock(EmailDistributionRepositoryAccessor.class);
        final HipChatDistributionRepositoryAccessor hipChatDistributionRepositoryAccessor = Mockito.mock(HipChatDistributionRepositoryAccessor.class);
        final SlackDistributionRepositoryAccessor slackDistributionRepositoryAccessor = Mockito.mock(SlackDistributionRepositoryAccessor.class);

        final Long commonDistributionConfigId = 25L;
        final Long distributionConfigId = 33L;
        final String distributionType = EmailGroupChannel.COMPONENT_NAME;
        final String providerName = BlackDuckProvider.COMPONENT_NAME;

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

        DatabaseEntity projectEntity1 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity("Project one", "", ""));
        DatabaseEntity projectEntity2 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity("Project two", "", ""));
        DatabaseEntity projectEntity3 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity("Project three", "", ""));
        DatabaseEntity projectEntity4 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity("Project four", "", ""));

        String email1 = "Test Email 1";
        String email2 = "Test Email 2";
        DatabaseEntity userEntity1 = blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity(email1, false));
        DatabaseEntity userEntity2 = blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity(email2, false));

        UserProjectRelation userProjectRelation1 = new UserProjectRelation(userEntity1.getId(), projectEntity1.getId());
        UserProjectRelation userProjectRelation2 = new UserProjectRelation(userEntity1.getId(), projectEntity2.getId());
        UserProjectRelation userProjectRelation3 = new UserProjectRelation(userEntity2.getId(), projectEntity3.getId());
        UserProjectRelation userProjectRelation4 = new UserProjectRelation(userEntity2.getId(), projectEntity4.getId());

        userProjectRelationRepositoryAccessor.deleteAndSaveAll(new HashSet<>(Arrays.asList(userProjectRelation1, userProjectRelation2, userProjectRelation3, userProjectRelation4)));

        final ChannelEventFactory factory = new ChannelEventFactory(emailDistributionRepositoryAccessor, hipChatDistributionRepositoryAccessor, slackDistributionRepositoryAccessor,
            blackDuckProjectRepositoryAccessor, blackDuckUserRepositoryAccessor, userProjectRelationRepositoryAccessor);

        AggregateMessageContent testContent = createTestNotificationContent();

        final EmailChannelEvent expectedTest = new EmailChannelEvent(RestConstants.formatDate(new Date()), providerName,
            testContent, commonDistributionConfigId, new HashSet<>(Arrays.asList(email1, email2)), subjectLine);

        EmailDistributionConfig emailDistributionConfig = new EmailDistributionConfig("1", "1", distributionType, "Test Email Job", BlackDuckProvider.COMPONENT_NAME, "REAL_TIME", "false",
            "", subjectLine, Collections.emptyList(), Collections.emptyList(), "DEFAULT");

        final EmailChannelEvent testEvent = factory.createEmailChannelTestEvent(emailDistributionConfig);
        assertEquals(expectedTest.getAuditEntryId(), testEvent.getAuditEntryId());
        assertEquals(expectedTest.getDestination(), testEvent.getDestination());
        assertEquals(expectedTest.getProvider(), testEvent.getProvider());
        assertEquals(expectedTest.getContent(), testEvent.getContent());
        assertEquals(expectedTest.getSubjectLine(), testEvent.getSubjectLine());
        assertEquals(expectedTest.getEmailAddresses(), testEvent.getEmailAddresses());

    }

    @Test
    public void createTestEmailEventWithEmailAddresses() throws Exception {
        final EmailDistributionRepositoryAccessor emailDistributionRepositoryAccessor = Mockito.mock(EmailDistributionRepositoryAccessor.class);
        final HipChatDistributionRepositoryAccessor hipChatDistributionRepositoryAccessor = Mockito.mock(HipChatDistributionRepositoryAccessor.class);
        final SlackDistributionRepositoryAccessor slackDistributionRepositoryAccessor = Mockito.mock(SlackDistributionRepositoryAccessor.class);

        final Long commonDistributionConfigId = 25L;
        final Long distributionConfigId = 33L;
        final String distributionType = EmailGroupChannel.COMPONENT_NAME;
        final String providerName = BlackDuckProvider.COMPONENT_NAME;

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

        String project1 = "Project one";
        String project2 = "Project two";
        DatabaseEntity projectEntity1 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity(project1, "", ""));
        DatabaseEntity projectEntity2 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity(project2, "", ""));

        String email1 = "Test Email 1";
        String email2 = "Test Email 2";
        String email3 = "Test Email 3";
        String email4 = "Test Email 4";
        DatabaseEntity userEntity1 = blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity(email1, false));
        DatabaseEntity userEntity2 = blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity(email2, false));
        DatabaseEntity userEntity3 = blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity(email3, false));
        DatabaseEntity userEntity4 = blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity(email4, false));

        UserProjectRelation userProjectRelation1 = new UserProjectRelation(userEntity1.getId(), projectEntity1.getId());
        UserProjectRelation userProjectRelation2 = new UserProjectRelation(userEntity2.getId(), projectEntity2.getId());
        UserProjectRelation userProjectRelation3 = new UserProjectRelation(userEntity3.getId(), projectEntity2.getId());
        UserProjectRelation userProjectRelation4 = new UserProjectRelation(userEntity4.getId(), projectEntity2.getId());

        userProjectRelationRepositoryAccessor.deleteAndSaveAll(new HashSet<>(Arrays.asList(userProjectRelation1, userProjectRelation2, userProjectRelation3, userProjectRelation4)));

        final ChannelEventFactory factory = new ChannelEventFactory(emailDistributionRepositoryAccessor, hipChatDistributionRepositoryAccessor, slackDistributionRepositoryAccessor,
            blackDuckProjectRepositoryAccessor, blackDuckUserRepositoryAccessor, userProjectRelationRepositoryAccessor);

        AggregateMessageContent testContent = createTestNotificationContent();

        final EmailChannelEvent expectedTest = new EmailChannelEvent(RestConstants.formatDate(new Date()), providerName,
            testContent, commonDistributionConfigId, new HashSet<>(Arrays.asList(email1, email2, email3, email4)), subjectLine);

        EmailDistributionConfig emailDistributionConfig = new EmailDistributionConfig("1", "1", distributionType, "Test Email Job", BlackDuckProvider.COMPONENT_NAME, "REAL_TIME", "true",
            "", subjectLine, Arrays.asList(project1, project2), Collections.emptyList(), "DEFAULT");

        final EmailChannelEvent testEvent = factory.createEmailChannelTestEvent(emailDistributionConfig);
        assertEquals(expectedTest.getAuditEntryId(), testEvent.getAuditEntryId());
        assertEquals(expectedTest.getDestination(), testEvent.getDestination());
        assertEquals(expectedTest.getProvider(), testEvent.getProvider());
        assertEquals(expectedTest.getContent(), testEvent.getContent());
        assertEquals(expectedTest.getSubjectLine(), testEvent.getSubjectLine());
        assertEquals(expectedTest.getEmailAddresses(), testEvent.getEmailAddresses());

    }

    @Test
    public void createHipChatEvent() throws Exception {
        final EmailDistributionRepositoryAccessor emailDistributionRepositoryAccessor = Mockito.mock(EmailDistributionRepositoryAccessor.class);
        final HipChatDistributionRepositoryAccessor hipChatDistributionRepositoryAccessor = Mockito.mock(HipChatDistributionRepositoryAccessor.class);
        final SlackDistributionRepositoryAccessor slackDistributionRepositoryAccessor = Mockito.mock(SlackDistributionRepositoryAccessor.class);

        final Long commonDistributionConfigId = 25L;
        final Long distributionConfigId = 33L;
        final String distributionType = HipChatChannel.COMPONENT_NAME;
        final String providerName = BlackDuckProvider.COMPONENT_NAME;

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
            blackDuckProjectRepositoryAccessor, blackDuckUserRepositoryAccessor, userProjectRelationRepositoryAccessor);

        final LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        final AggregateMessageContent content = new AggregateMessageContent("testTopic", "", null, subTopic, Collections.emptyList());

        final HipChatChannelEvent expected = new HipChatChannelEvent(RestConstants.formatDate(new Date()), providerName,
            content, commonDistributionConfigId, roomId, notify, color);

        final HipChatChannelEvent event = (HipChatChannelEvent) factory.createChannelEvent(jobConfig, content);
        assertEquals(expected.getAuditEntryId(), event.getAuditEntryId());
        assertEquals(expected.getDestination(), event.getDestination());
        assertEquals(expected.getProvider(), event.getProvider());
        assertEquals(expected.getContent(), event.getContent());
        assertEquals(expected.getRoomId(), event.getRoomId());
        assertEquals(expected.getNotify(), event.getNotify());
        assertEquals(expected.getColor(), event.getColor());

        AggregateMessageContent testContent = createTestNotificationContent();

        final HipChatChannelEvent expectedTest = new HipChatChannelEvent(RestConstants.formatDate(new Date()), providerName,
            testContent, commonDistributionConfigId, roomId, notify, color);

        HipChatDistributionConfig hipChatDistributionConfig = new HipChatDistributionConfig("1", roomId.toString(), notify, color, "1", distributionType, "Test HipChat Job", providerName,
            "REAL_TIME", "FALSE", Collections.emptyList(), Collections.emptyList(), "DEFAULT");

        final HipChatChannelEvent testEvent = factory.createHipChatChannelTestEvent(hipChatDistributionConfig);
        assertEquals(expectedTest.getAuditEntryId(), testEvent.getAuditEntryId());
        assertEquals(expectedTest.getDestination(), testEvent.getDestination());
        assertEquals(expectedTest.getProvider(), testEvent.getProvider());
        assertEquals(expectedTest.getContent(), testEvent.getContent());
        assertEquals(expectedTest.getRoomId(), testEvent.getRoomId());
        assertEquals(expectedTest.getNotify(), testEvent.getNotify());
        assertEquals(expectedTest.getColor(), testEvent.getColor());
    }

    @Test
    public void createSlackEvent() throws Exception {
        final EmailDistributionRepositoryAccessor emailDistributionRepositoryAccessor = Mockito.mock(EmailDistributionRepositoryAccessor.class);
        final HipChatDistributionRepositoryAccessor hipChatDistributionRepositoryAccessor = Mockito.mock(HipChatDistributionRepositoryAccessor.class);
        final SlackDistributionRepositoryAccessor slackDistributionRepositoryAccessor = Mockito.mock(SlackDistributionRepositoryAccessor.class);

        final Long commonDistributionConfigId = 25L;
        final Long distributionConfigId = 33L;
        final String distributionType = SlackChannel.COMPONENT_NAME;
        final String providerName = BlackDuckProvider.COMPONENT_NAME;

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
            blackDuckProjectRepositoryAccessor, blackDuckUserRepositoryAccessor, userProjectRelationRepositoryAccessor);

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

        AggregateMessageContent testContent = createTestNotificationContent();

        final SlackChannelEvent expectedTest = new SlackChannelEvent(RestConstants.formatDate(new Date()), providerName,
            testContent, commonDistributionConfigId, channelUsername, webhook, channelName);

        SlackDistributionConfig slackDistributionConfig = new SlackDistributionConfig("1", webhook, channelUsername, channelName, "1", distributionType, "Test HipChat Job", providerName,
            "REAL_TIME", "FALSE", Collections.emptyList(), Collections.emptyList(), "DEFAULT");

        final SlackChannelEvent testEvent = factory.createSlackChannelTestEvent(slackDistributionConfig);
        assertEquals(expectedTest.getAuditEntryId(), testEvent.getAuditEntryId());
        assertEquals(expectedTest.getDestination(), testEvent.getDestination());
        assertEquals(expectedTest.getProvider(), testEvent.getProvider());
        assertEquals(expectedTest.getContent(), testEvent.getContent());
        assertEquals(expectedTest.getChannelUsername(), testEvent.getChannelUsername());
        assertEquals(expectedTest.getWebHook(), testEvent.getWebHook());
        assertEquals(expectedTest.getChannelName(), testEvent.getChannelName());
    }

    private AggregateMessageContent createTestNotificationContent() {
        final LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        return new AggregateMessageContent("testTopic", "Alert Test Message", null, subTopic, Collections.emptyList());

    }
}
