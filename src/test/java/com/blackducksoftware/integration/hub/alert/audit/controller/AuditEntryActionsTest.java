package com.blackducksoftware.integration.hub.alert.audit.controller;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.NotificationManager;
import com.blackducksoftware.integration.hub.alert.OutputLogger;
import com.blackducksoftware.integration.hub.alert.audit.mock.MockAuditEntryEntity;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditEntryRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditNotificationRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.NotificationRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.VulnerabilityRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockCommonDistributionEntity;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockNotificationEntity;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;

public class AuditEntryActionsTest {
    private OutputLogger outputLogger;

    @Before
    public void init() throws IOException {
        outputLogger = new OutputLogger();
    }

    @After
    public void cleanup() throws IOException {
        outputLogger.cleanup();
    }

    @Test
    public void testGetNull() {
        final AuditEntryRepositoryWrapper auditEntryRepository = Mockito.mock(AuditEntryRepositoryWrapper.class);
        Mockito.when(auditEntryRepository.findOne(Mockito.anyLong())).thenReturn(null);
        final AuditEntryActions auditEntryActions = new AuditEntryActions(auditEntryRepository, null, null, null, null, null, null, null);

        final AuditEntryRestModel restModel = auditEntryActions.get(1L);
        assertNull(restModel);
    }

    @Test
    public void testGetException() throws AlertException, IOException {
        final AuditEntryRepositoryWrapper auditEntryRepository = Mockito.mock(AuditEntryRepositoryWrapper.class);
        final NotificationRepositoryWrapper notificationRepository = Mockito.mock(NotificationRepositoryWrapper.class);
        final VulnerabilityRepositoryWrapper vulnerabilityRepository = Mockito.mock(VulnerabilityRepositoryWrapper.class);
        final AuditNotificationRepositoryWrapper auditNotificationRepository = Mockito.mock(AuditNotificationRepositoryWrapper.class);
        final CommonDistributionRepositoryWrapper commonDistributionRepositoryWrapper = Mockito.mock(CommonDistributionRepositoryWrapper.class);
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final ObjectTransformer spyObjectTransformer = Mockito.spy(objectTransformer);
        final MockAuditEntryEntity mockAuditEntryEntity = new MockAuditEntryEntity();
        final MockNotificationEntity mockNotificationEntity = new MockNotificationEntity();
        final MockCommonDistributionEntity mockCommonDistributionEntity = new MockCommonDistributionEntity();
        Mockito.when(auditEntryRepository.findOne(Mockito.anyLong())).thenReturn(mockAuditEntryEntity.createEmptyEntity());
        Mockito.when(commonDistributionRepositoryWrapper.findOne(Mockito.anyLong())).thenReturn(mockCommonDistributionEntity.createEntity());
        Mockito.doThrow(new AlertException()).when(spyObjectTransformer).databaseEntityToConfigRestModel(Mockito.any(), Mockito.any());
        Mockito.when(notificationRepository.findAll(Mockito.any())).thenReturn(Arrays.asList(mockNotificationEntity.createEntity()));
        final AuditEntryActions auditEntryActions = new AuditEntryActions(auditEntryRepository, new NotificationManager(notificationRepository, vulnerabilityRepository, auditEntryRepository, auditNotificationRepository),
                auditNotificationRepository,
                commonDistributionRepositoryWrapper,
                spyObjectTransformer, null, null, null);

        auditEntryActions.get(1L);

        assertTrue(outputLogger.isLineContainingText("Problem converting audit entry"));
    }

    @Test
    public void testResendNotificationException() {
        final AuditEntryRepositoryWrapper auditEntryRepository = Mockito.mock(AuditEntryRepositoryWrapper.class);
        final NotificationRepositoryWrapper notificationRepository = Mockito.mock(NotificationRepositoryWrapper.class);
        final VulnerabilityRepositoryWrapper vulnerabilityRepository = Mockito.mock(VulnerabilityRepositoryWrapper.class);
        final AuditNotificationRepositoryWrapper auditNotificationRepository = Mockito.mock(AuditNotificationRepositoryWrapper.class);
        final CommonDistributionRepositoryWrapper commonDistributionRepositoryWrapper = Mockito.mock(CommonDistributionRepositoryWrapper.class);
        final MockAuditEntryEntity mockAuditEntryEntity = new MockAuditEntryEntity();
        final MockNotificationEntity mockNotificationEntity = new MockNotificationEntity();
        Mockito.when(auditEntryRepository.findOne(Mockito.anyLong())).thenReturn(mockAuditEntryEntity.createEmptyEntity());
        Mockito.when(commonDistributionRepositoryWrapper.findOne(Mockito.anyLong())).thenReturn(null);
        Mockito.when(notificationRepository.findAll(Mockito.any())).thenReturn(Arrays.asList(mockNotificationEntity.createEntity()));
        final AuditEntryActions auditEntryActions = new AuditEntryActions(auditEntryRepository, new NotificationManager(notificationRepository, vulnerabilityRepository, auditEntryRepository, auditNotificationRepository),
                auditNotificationRepository, commonDistributionRepositoryWrapper,
                null, null, null, null);

        List<AuditEntryRestModel> restModel = null;
        try {
            restModel = auditEntryActions.resendNotification(1L);
            fail();
        } catch (final IllegalArgumentException e) {
            assertTrue(true);
        } catch (final IntegrationException e) {
            fail();
        }

        assertNull(restModel);
    }

}
