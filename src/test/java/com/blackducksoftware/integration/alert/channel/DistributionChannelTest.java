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
package com.blackducksoftware.integration.alert.channel;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.alert.TestAlertProperties;
import com.blackducksoftware.integration.alert.TestBlackDuckProperties;
import com.blackducksoftware.integration.alert.channel.email.EmailGroupChannel;
import com.blackducksoftware.integration.alert.channel.email.mock.MockEmailGlobalEntity;
import com.blackducksoftware.integration.alert.channel.event.ChannelEvent;
import com.blackducksoftware.integration.alert.channel.slack.SlackChannel;
import com.blackducksoftware.integration.alert.common.digest.model.DigestModel;
import com.blackducksoftware.integration.alert.common.enumeration.AuditEntryStatus;
import com.blackducksoftware.integration.alert.common.enumeration.DigestType;
import com.blackducksoftware.integration.alert.common.exception.AlertException;
import com.blackducksoftware.integration.alert.database.audit.AuditEntryEntity;
import com.blackducksoftware.integration.alert.database.audit.AuditEntryRepository;
import com.blackducksoftware.integration.alert.database.channel.email.EmailGlobalConfigEntity;
import com.blackducksoftware.integration.alert.database.channel.email.EmailGlobalRepository;
import com.blackducksoftware.integration.alert.database.channel.email.EmailGroupDistributionConfigEntity;
import com.blackducksoftware.integration.alert.database.channel.email.EmailGroupDistributionRepository;
import com.blackducksoftware.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.alert.database.entity.channel.GlobalChannelConfigEntity;
import com.blackducksoftware.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackducksoftware.integration.exception.IntegrationException;

public class DistributionChannelTest extends ChannelTest {
    @Test
    public void setAuditEntrySuccessCatchExceptionTest() {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final BlackDuckProperties hubProperties = new TestBlackDuckProperties(testAlertProperties);
        final EmailGroupChannel channel = new EmailGroupChannel(gson, testAlertProperties, hubProperties, null, null, null, null, contentConverter);
        channel.setAuditEntrySuccess(1L);
    }

    @Test
    public void setAuditEntrySuccessTest() {
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final BlackDuckProperties hubProperties = new TestBlackDuckProperties(testAlertProperties);
        final EmailGroupChannel channel = new EmailGroupChannel(gson, testAlertProperties, hubProperties, auditEntryRepository, null, null, null, contentConverter);

        final AuditEntryEntity entity = new AuditEntryEntity(1L, new Date(System.currentTimeMillis() - 1000), new Date(System.currentTimeMillis()), AuditEntryStatus.SUCCESS, null, null);
        entity.setId(1L);
        Mockito.when(auditEntryRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(entity));
        Mockito.when(auditEntryRepository.save(entity)).thenReturn(entity);

        channel.setAuditEntrySuccess(null);
        channel.setAuditEntrySuccess(entity.getId());
    }

    @Test
    public void setAuditEntryFailureCatchExceptionTest() {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final BlackDuckProperties hubProperties = new TestBlackDuckProperties(testAlertProperties);
        final EmailGroupChannel channel = new EmailGroupChannel(gson, testAlertProperties, hubProperties, null, null, null, null, contentConverter);
        channel.setAuditEntryFailure(1L, null, null);
    }

    @Test
    public void setAuditEntryFailureTest() {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final BlackDuckProperties hubProperties = new TestBlackDuckProperties(testAlertProperties);
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        final EmailGroupChannel channel = new EmailGroupChannel(gson, testAlertProperties, hubProperties, auditEntryRepository, null, null, null, contentConverter);
        final AuditEntryEntity entity = new AuditEntryEntity(1L, new Date(System.currentTimeMillis() - 1000), new Date(System.currentTimeMillis()), AuditEntryStatus.FAILURE, null, null);
        entity.setId(1L);
        Mockito.when(auditEntryRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(entity));
        Mockito.when(auditEntryRepository.save(entity)).thenReturn(entity);

        channel.setAuditEntryFailure(null, null, null);
        channel.setAuditEntryFailure(entity.getId(), "error", new Exception());
    }

    @Test
    public void getGlobalConfigEntityTest() {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final BlackDuckProperties hubProperties = new TestBlackDuckProperties(testAlertProperties);
        final EmailGlobalRepository emailGlobalRepository = Mockito.mock(EmailGlobalRepository.class);
        final EmailGroupChannel channel = new EmailGroupChannel(gson, testAlertProperties, hubProperties, null, emailGlobalRepository, null, null, contentConverter);

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
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        final EmailGlobalRepository emailGlobalRepository = Mockito.mock(EmailGlobalRepository.class);
        final EmailGroupDistributionRepository emailGroupRepository = Mockito.mock(EmailGroupDistributionRepository.class);
        final CommonDistributionRepository commonRepository = Mockito.mock(CommonDistributionRepository.class);

        final EmailGroupChannel channel = new EmailGroupChannel(gson, testAlertProperties, hubProperties, auditEntryRepository, emailGlobalRepository, emailGroupRepository, commonRepository, contentConverter);

        final Long commonId = 1L;
        final DigestModel digestModel = new DigestModel(createProjectData("Distribution Channel Test"));
        final ChannelEvent event = new ChannelEvent(EmailGroupChannel.COMPONENT_NAME, contentConverter.getJsonString(digestModel), commonId);

        final CommonDistributionConfigEntity commonEntity = new CommonDistributionConfigEntity(commonId, EmailGroupChannel.COMPONENT_NAME, "Email Config", DigestType.REAL_TIME, false);
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
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final BlackDuckProperties hubProperties = new TestBlackDuckProperties(testAlertProperties);
        final CommonDistributionRepository commonRepository = Mockito.mock(CommonDistributionRepository.class);

        final EmailGroupChannel channel = new EmailGroupChannel(gson, testAlertProperties, hubProperties, null, null, null, commonRepository, contentConverter);

        final Long commonId = 1L;
        final DigestModel digestModel = new DigestModel(createProjectData("Distribution Channel Test"));
        final ChannelEvent event = new ChannelEvent(EmailGroupChannel.COMPONENT_NAME, contentConverter.getJsonString(digestModel), commonId);

        final CommonDistributionConfigEntity commonEntity = new CommonDistributionConfigEntity(commonId, SlackChannel.COMPONENT_NAME, "Other Config", DigestType.REAL_TIME, false);
        Mockito.when(commonRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(commonEntity));

        channel.handleEvent(event);
    }

    @Test
    public void testGlobalConfigTest() throws IntegrationException {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final BlackDuckProperties hubProperties = new TestBlackDuckProperties(testAlertProperties);
        // Slack has no global config, so we use it to test the default method.
        final SlackChannel slackChannel = new SlackChannel(gson, testAlertProperties, hubProperties, null, null, null, null, contentConverter);
        final GlobalChannelConfigEntity globalChannelConfigEntity = Mockito.mock(GlobalChannelConfigEntity.class);

        final String nullMessage = slackChannel.testGlobalConfig(null);
        assertEquals("The provided entity was null.", nullMessage);
        try {
            final String validEntityMessage = slackChannel.testGlobalConfig(globalChannelConfigEntity);
        } catch (final AlertException ex) {
            assertEquals("Test method not implemented.", ex.getMessage());
        }
    }

}
