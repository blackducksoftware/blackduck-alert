package com.blackducksoftware.integration.hub.alert.audit.controller;

import static org.junit.Assert.assertEquals;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.NotificationManager;
import com.blackducksoftware.integration.hub.alert.OutputLogger;
import com.blackducksoftware.integration.hub.alert.audit.mock.MockAuditEntryEntity;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditEntryEntity;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditEntryRepository;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditEntryRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditNotificationRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.NotificationRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.VulnerabilityRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockCommonDistributionEntity;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockNotificationEntity;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.AlertPagedRestModel;

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
        Mockito.when(notificationRepository.findAll(Mockito.anyListOf(Long.class))).thenReturn(Arrays.asList(mockNotificationEntity.createEntity()));
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
        Mockito.when(notificationRepository.findAll(Mockito.anyListOf(Long.class))).thenReturn(Arrays.asList(mockNotificationEntity.createEntity()));
        final AuditEntryActions auditEntryActions = new AuditEntryActions(auditEntryRepository, new NotificationManager(notificationRepository, vulnerabilityRepository, auditEntryRepository, auditNotificationRepository),
                auditNotificationRepository, commonDistributionRepositoryWrapper,
                null, null, null, null);

        AlertPagedRestModel<AuditEntryRestModel> restModel = null;
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

    @Test
    public void testPagedRequest() {
        final int totalPages = 2;
        final int currentPage = 1;
        final int pageSize = 2;

        final AuditEntryEntity entity_1 = new AuditEntryEntity();
        entity_1.setId(1L);
        final AuditEntryEntity entity_2 = new AuditEntryEntity();
        entity_2.setId(2L);
        final List<AuditEntryEntity> pagedEntryList = Arrays.asList(entity_1, entity_2);
        final Page<AuditEntryEntity> pageResponse = Mockito.mock(Page.class);

        Mockito.when(pageResponse.getContent()).thenReturn(pagedEntryList);
        Mockito.when(pageResponse.getTotalPages()).thenReturn(totalPages);
        Mockito.when(pageResponse.getNumber()).thenReturn(currentPage);
        Mockito.when(pageResponse.getSize()).thenReturn(pageSize);

        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        Mockito.when(auditEntryRepository.findAll(Mockito.any(PageRequest.class))).thenReturn(pageResponse);

        final AuditEntryRepositoryWrapper auditEntryRepositoryWrapper = new AuditEntryRepositoryWrapper(auditEntryRepository);
        final NotificationRepositoryWrapper notificationRepository = Mockito.mock(NotificationRepositoryWrapper.class);
        final VulnerabilityRepositoryWrapper vulnerabilityRepository = Mockito.mock(VulnerabilityRepositoryWrapper.class);
        final AuditNotificationRepositoryWrapper auditNotificationRepository = Mockito.mock(AuditNotificationRepositoryWrapper.class);
        final CommonDistributionRepositoryWrapper commonDistributionRepositoryWrapper = Mockito.mock(CommonDistributionRepositoryWrapper.class);
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final ObjectTransformer spyObjectTransformer = Mockito.spy(objectTransformer);
        final MockNotificationEntity mockNotificationEntity = new MockNotificationEntity();
        final MockCommonDistributionEntity mockCommonDistributionEntity = new MockCommonDistributionEntity();
        Mockito.when(commonDistributionRepositoryWrapper.findAll()).thenReturn(Arrays.asList(mockCommonDistributionEntity.createEntity()));
        Mockito.when(notificationRepository.findAll(Mockito.anyListOf(Long.class))).thenReturn(Arrays.asList(mockNotificationEntity.createEntity()));
        final AuditEntryActions auditEntryActions = new AuditEntryActions(auditEntryRepositoryWrapper, new NotificationManager(notificationRepository, vulnerabilityRepository, auditEntryRepositoryWrapper, auditNotificationRepository),
                auditNotificationRepository, commonDistributionRepositoryWrapper,
                spyObjectTransformer, null, null, null);

        final AlertPagedRestModel<AuditEntryRestModel> restModel = auditEntryActions.get(currentPage, pageSize);
        assertEquals(pageResponse.getTotalPages(), restModel.getTotalPages());
        assertEquals(pageResponse.getNumber(), restModel.getCurrentPage());
        assertEquals(pageResponse.getSize(), restModel.getPageSize());

        for (int index = 0; index < pageSize; index++) {
            final AuditEntryEntity entity = pageResponse.getContent().get(index);
            final AuditEntryRestModel entryRestModel = restModel.getContent().get(index);
            assertEquals(String.valueOf(entity.getId()), entryRestModel.getId());
        }
    }

    @Test
    public void testPagedRequestEmptyList() {
        final int totalPages = 1;
        final int currentPage = 1;
        final int pageSize = 1;
        final Page<AuditEntryEntity> pageResponse = Mockito.mock(Page.class);

        Mockito.when(pageResponse.getContent()).thenReturn(null);
        Mockito.when(pageResponse.getTotalPages()).thenReturn(totalPages);
        Mockito.when(pageResponse.getNumber()).thenReturn(currentPage);
        Mockito.when(pageResponse.getSize()).thenReturn(pageSize);

        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        Mockito.when(auditEntryRepository.findAll(Mockito.any(PageRequest.class))).thenReturn(pageResponse);

        final AuditEntryRepositoryWrapper auditEntryRepositoryWrapper = new AuditEntryRepositoryWrapper(auditEntryRepository);
        final NotificationRepositoryWrapper notificationRepository = Mockito.mock(NotificationRepositoryWrapper.class);
        final VulnerabilityRepositoryWrapper vulnerabilityRepository = Mockito.mock(VulnerabilityRepositoryWrapper.class);
        final AuditNotificationRepositoryWrapper auditNotificationRepository = Mockito.mock(AuditNotificationRepositoryWrapper.class);
        final CommonDistributionRepositoryWrapper commonDistributionRepositoryWrapper = Mockito.mock(CommonDistributionRepositoryWrapper.class);
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final ObjectTransformer spyObjectTransformer = Mockito.spy(objectTransformer);
        final MockNotificationEntity mockNotificationEntity = new MockNotificationEntity();
        final MockCommonDistributionEntity mockCommonDistributionEntity = new MockCommonDistributionEntity();
        Mockito.when(commonDistributionRepositoryWrapper.findAll()).thenReturn(Arrays.asList(mockCommonDistributionEntity.createEntity()));
        Mockito.when(notificationRepository.findAll(Mockito.anyListOf(Long.class))).thenReturn(Arrays.asList(mockNotificationEntity.createEntity()));
        final AuditEntryActions auditEntryActions = new AuditEntryActions(auditEntryRepositoryWrapper, new NotificationManager(notificationRepository, vulnerabilityRepository, auditEntryRepositoryWrapper, auditNotificationRepository),
                auditNotificationRepository, commonDistributionRepositoryWrapper,
                spyObjectTransformer, null, null, null);

        final AlertPagedRestModel<AuditEntryRestModel> restModel = auditEntryActions.get(currentPage, pageSize);
        assertEquals(pageResponse.getTotalPages(), restModel.getTotalPages());
        assertEquals(pageResponse.getNumber(), restModel.getCurrentPage());
        assertEquals(pageResponse.getSize(), restModel.getPageSize());
        assertTrue(restModel.getContent().isEmpty());
    }

    @Test
    public void testPageRequestDecryptionException() {
        try {
            final int totalPages = 2;
            final int currentPage = 1;
            final int pageSize = 2;

            final AuditEntryEntity entity_1 = new AuditEntryEntity();
            entity_1.setId(1L);
            final AuditEntryEntity entity_2 = new AuditEntryEntity();
            entity_2.setId(2L);
            final List<AuditEntryEntity> pagedEntryList = Arrays.asList(entity_1, entity_2);
            final Page<AuditEntryEntity> pageResponse = Mockito.mock(Page.class);

            Mockito.when(pageResponse.getContent()).thenReturn(pagedEntryList);
            Mockito.when(pageResponse.getTotalPages()).thenReturn(totalPages);
            Mockito.when(pageResponse.getNumber()).thenReturn(currentPage);
            Mockito.when(pageResponse.getSize()).thenReturn(pageSize);

            final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
            Mockito.when(auditEntryRepository.findAll(Mockito.any(PageRequest.class))).thenReturn(pageResponse);

            final AuditEntryRepositoryWrapper auditEntryRepositoryWrapper = new AuditEntryRepositoryWrapper(auditEntryRepository);
            final AuditEntryRepositoryWrapper spyRepositoryWrapper = Mockito.spy(auditEntryRepositoryWrapper);
            Mockito.when(spyRepositoryWrapper.decryptSensitiveData(Mockito.any(AuditEntryEntity.class))).thenThrow(EncryptionException.class);
            final NotificationRepositoryWrapper notificationRepository = Mockito.mock(NotificationRepositoryWrapper.class);
            final VulnerabilityRepositoryWrapper vulnerabilityRepository = Mockito.mock(VulnerabilityRepositoryWrapper.class);
            final AuditNotificationRepositoryWrapper auditNotificationRepository = Mockito.mock(AuditNotificationRepositoryWrapper.class);
            final CommonDistributionRepositoryWrapper commonDistributionRepositoryWrapper = Mockito.mock(CommonDistributionRepositoryWrapper.class);
            final ObjectTransformer objectTransformer = new ObjectTransformer();
            final ObjectTransformer spyObjectTransformer = Mockito.spy(objectTransformer);
            final MockNotificationEntity mockNotificationEntity = new MockNotificationEntity();
            final MockCommonDistributionEntity mockCommonDistributionEntity = new MockCommonDistributionEntity();
            Mockito.when(commonDistributionRepositoryWrapper.findAll()).thenReturn(Arrays.asList(mockCommonDistributionEntity.createEntity()));
            Mockito.when(notificationRepository.findAll(Mockito.anyListOf(Long.class))).thenReturn(Arrays.asList(mockNotificationEntity.createEntity()));
            final AuditEntryActions auditEntryActions = new AuditEntryActions(spyRepositoryWrapper, new NotificationManager(notificationRepository, vulnerabilityRepository, auditEntryRepositoryWrapper, auditNotificationRepository),
                    auditNotificationRepository, commonDistributionRepositoryWrapper,
                    spyObjectTransformer, null, null, null);

            final AlertPagedRestModel<AuditEntryRestModel> restModel = auditEntryActions.get(currentPage, pageSize);
            assertEquals(pageResponse.getTotalPages(), restModel.getTotalPages());
            assertEquals(pageResponse.getNumber(), restModel.getCurrentPage());
            assertEquals(pageResponse.getSize(), restModel.getPageSize());
            assertTrue(restModel.getContent().isEmpty());
        } catch (final EncryptionException ex) {
            fail();
        }
    }
}
