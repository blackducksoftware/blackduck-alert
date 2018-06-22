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
package com.blackducksoftware.integration.hub.alert.channel;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditEntryEntity;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditEntryRepository;
import com.blackducksoftware.integration.hub.alert.channel.email.EmailGroupChannel;
import com.blackducksoftware.integration.hub.alert.channel.email.mock.MockEmailGlobalEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.distribution.EmailGroupDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.distribution.EmailGroupDistributionRepository;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailRepository;
import com.blackducksoftware.integration.hub.alert.channel.slack.SlackChannel;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.global.GlobalSlackConfigEntity;
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.hub.alert.digest.model.DigestModel;
import com.blackducksoftware.integration.hub.alert.enumeration.DigestTypeEnum;
import com.blackducksoftware.integration.hub.alert.enumeration.StatusEnum;
import com.blackducksoftware.integration.hub.alert.event.ChannelEvent;

public class DistributionChannelTest extends ChannelTest {
    @Test
    public void setAuditEntrySuccessCatchExceptionTest() {
        final EmailGroupChannel channel = new EmailGroupChannel(gson, null, null, null, null, null, contentConverter);
        channel.setAuditEntrySuccess(1L);
    }

    @Test
    public void setAuditEntrySuccessTest() {
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        final EmailGroupChannel channel = new EmailGroupChannel(gson, null, auditEntryRepository, null, null, null, contentConverter);

        final AuditEntryEntity entity = new AuditEntryEntity(1L, new Date(System.currentTimeMillis() - 1000), new Date(System.currentTimeMillis()), StatusEnum.SUCCESS, null, null);
        entity.setId(1L);
        Mockito.when(auditEntryRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(entity));
        Mockito.when(auditEntryRepository.save(entity)).thenReturn(entity);

        channel.setAuditEntrySuccess(null);
        channel.setAuditEntrySuccess(entity.getId());
    }

    @Test
    public void setAuditEntryFailureCatchExceptionTest() {
        final EmailGroupChannel channel = new EmailGroupChannel(gson, null, null, null, null, null, contentConverter);
        channel.setAuditEntryFailure(1L, null, null);
    }

    @Test
    public void setAuditEntryFailureTest() {
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        final EmailGroupChannel channel = new EmailGroupChannel(gson, null, auditEntryRepository, null, null, null, contentConverter);
        final AuditEntryEntity entity = new AuditEntryEntity(1L, new Date(System.currentTimeMillis() - 1000), new Date(System.currentTimeMillis()), StatusEnum.FAILURE, null, null);
        entity.setId(1L);
        Mockito.when(auditEntryRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(entity));
        Mockito.when(auditEntryRepository.save(entity)).thenReturn(entity);

        channel.setAuditEntryFailure(null, null, null);
        channel.setAuditEntryFailure(entity.getId(), "error", new Exception());
    }

    @Test
    public void getGlobalConfigEntityTest() {
        final GlobalEmailRepository globalEmailRepository = Mockito.mock(GlobalEmailRepository.class);
        final EmailGroupChannel channel = new EmailGroupChannel(gson, null, null, globalEmailRepository, null, null, contentConverter);

        final MockEmailGlobalEntity mockEntity = new MockEmailGlobalEntity();
        final GlobalEmailConfigEntity entity = mockEntity.createGlobalEntity();
        Mockito.when(globalEmailRepository.findAll()).thenReturn(Arrays.asList(entity));

        final GlobalEmailConfigEntity globalEntity = channel.getGlobalConfigEntity();
        assertEquals(entity, globalEntity);
    }

    @Test
    public void receiveMessageTest() {
        final GlobalProperties globalProperties = new TestGlobalProperties();
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        final GlobalEmailRepository globalEmailRepository = Mockito.mock(GlobalEmailRepository.class);
        final EmailGroupDistributionRepository emailGroupRepository = Mockito.mock(EmailGroupDistributionRepository.class);
        final CommonDistributionRepository commonRepository = Mockito.mock(CommonDistributionRepository.class);

        final EmailGroupChannel channel = new EmailGroupChannel(gson, globalProperties, auditEntryRepository, globalEmailRepository, emailGroupRepository, commonRepository, contentConverter);

        final Long commonId = 1L;
        final DigestModel digestModel = new DigestModel(createProjectData("Distribution Channel Test"));
        final ChannelEvent event = new ChannelEvent(EmailGroupChannel.COMPONENT_NAME, contentConverter.convertToString(digestModel), commonId);

        final CommonDistributionConfigEntity commonEntity = new CommonDistributionConfigEntity(commonId, EmailGroupChannel.COMPONENT_NAME, "Email Config", DigestTypeEnum.REAL_TIME, false);
        Mockito.when(commonRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(commonEntity));

        final EmailGroupDistributionConfigEntity specificEntity = new EmailGroupDistributionConfigEntity("admins", "", "TEST SUBJECT LINE");
        Mockito.when(emailGroupRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(specificEntity));

        channel.handleEvent(event);
    }

    // @Test
    // public void receiveMessageCatchExceptionTest() {
    // final EmailGroupChannel channel = new EmailGroupChannel(null, null, null, null, null);
    //
    // channel.handleEvent(null);
    // }

    @Test
    public void handleEventWrongTypeTest() {
        final GlobalProperties globalProperties = new TestGlobalProperties();
        final CommonDistributionRepository commonRepository = Mockito.mock(CommonDistributionRepository.class);

        final EmailGroupChannel channel = new EmailGroupChannel(gson, globalProperties, null, null, null, commonRepository, contentConverter);

        final Long commonId = 1L;
        final DigestModel digestModel = new DigestModel(createProjectData("Distribution Channel Test"));
        final ChannelEvent event = new ChannelEvent(EmailGroupChannel.COMPONENT_NAME, contentConverter.convertToString(digestModel), commonId);

        final CommonDistributionConfigEntity commonEntity = new CommonDistributionConfigEntity(commonId, SlackChannel.COMPONENT_NAME, "Other Config", DigestTypeEnum.REAL_TIME, false);
        Mockito.when(commonRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(commonEntity));

        channel.handleEvent(event);
    }

    @Test
    public void testGlobalConfigTest() throws IntegrationException {
        // Slack has no global config, so we use it to test the default method.
        final SlackChannel slackChannel = new SlackChannel(gson, null, null, null, null, contentConverter);

        final String nullMessage = slackChannel.testGlobalConfig(null);
        assertEquals("The provided entity was null.", nullMessage);

        final String validEntityMessage = slackChannel.testGlobalConfig(new GlobalSlackConfigEntity());
        assertEquals("Not implemented.", validEntityMessage);
    }

}
