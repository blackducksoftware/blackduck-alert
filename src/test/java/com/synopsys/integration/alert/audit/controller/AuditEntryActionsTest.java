package com.synopsys.integration.alert.audit.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.google.gson.Gson;
import com.synopsys.integration.alert.OutputLogger;
import com.synopsys.integration.alert.audit.mock.MockAuditEntryEntity;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.synopsys.integration.alert.database.entity.repository.NotificationContentRepository;
import com.synopsys.integration.alert.database.entity.repository.VulnerabilityRepository;
import com.synopsys.integration.alert.mock.entity.MockCommonDistributionEntity;
import com.synopsys.integration.alert.mock.entity.MockNotificationContent;
import com.synopsys.integration.alert.web.audit.AuditEntryActions;
import com.synopsys.integration.alert.web.audit.AuditEntryConfig;
import com.synopsys.integration.alert.web.model.AlertPagedModel;
import com.synopsys.integration.alert.web.model.NotificationContentConverter;
import com.synopsys.integration.alert.workflow.NotificationManager;
import com.synopsys.integration.exception.IntegrationException;

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
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        Mockito.when(auditEntryRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
        final AuditEntryActions auditEntryActions = new AuditEntryActions(auditEntryRepository, null, null, null, null, null, null, null);

        final AuditEntryConfig restModel = auditEntryActions.get(1L);
        assertNull(restModel);
    }

    @Test
    public void testResendNotificationException() {
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        final NotificationContentRepository notificationRepository = Mockito.mock(NotificationContentRepository.class);
        final VulnerabilityRepository vulnerabilityRepository = Mockito.mock(VulnerabilityRepository.class);
        final AuditNotificationRepository auditNotificationRepository = Mockito.mock(AuditNotificationRepository.class);
        final CommonDistributionRepository commonDistributionRepository = Mockito.mock(CommonDistributionRepository.class);
        final MockAuditEntryEntity mockAuditEntryEntity = new MockAuditEntryEntity();
        final MockNotificationContent mockNotificationEntity = new MockNotificationContent();
        Mockito.when(auditEntryRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(mockAuditEntryEntity.createEmptyEntity()));
        Mockito.when(commonDistributionRepository.findById(Mockito.anyLong())).thenReturn(null);
        Mockito.when(notificationRepository.findAllById(Mockito.anyList())).thenReturn(Arrays.asList(mockNotificationEntity.createEntity()));
        final AuditEntryActions auditEntryActions = new AuditEntryActions(auditEntryRepository, new NotificationManager(notificationRepository, vulnerabilityRepository, auditEntryRepository, auditNotificationRepository),
            auditNotificationRepository, commonDistributionRepository, null, null, null, null);

        AlertPagedModel<AuditEntryConfig> restModel = null;
        try {
            restModel = auditEntryActions.resendNotification(1L);
            fail();
        } catch (final IntegrationException e) {
        }

        assertNull(restModel);
    }

    @Test
    public void testPagedRequest() {
        final int totalPages = 2;
        final int currentPage = 0;
        final int pageSize = 2;

        final AuditEntryEntity entity_1 = new AuditEntryEntity();
        entity_1.setId(1L);
        final AuditEntryEntity entity_2 = new AuditEntryEntity();
        entity_2.setId(2L);
        final AuditEntryEntity entity_3 = new AuditEntryEntity();
        entity_2.setId(3L);
        final List<AuditEntryEntity> pagedEntryList = Arrays.asList(entity_1, entity_2, entity_3);
        @SuppressWarnings("unchecked") final Page<AuditEntryEntity> pageResponse = Mockito.mock(Page.class);

        Mockito.when(pageResponse.getContent()).thenReturn(pagedEntryList);
        Mockito.when(pageResponse.getTotalPages()).thenReturn(totalPages);
        Mockito.when(pageResponse.getNumber()).thenReturn(currentPage);
        Mockito.when(pageResponse.getSize()).thenReturn(pageSize);

        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        Mockito.when(auditEntryRepository.findAll(Mockito.any(PageRequest.class))).thenReturn(pageResponse);

        final NotificationContentRepository notificationRepository = Mockito.mock(NotificationContentRepository.class);
        final VulnerabilityRepository vulnerabilityRepository = Mockito.mock(VulnerabilityRepository.class);
        final AuditNotificationRepository auditNotificationRepository = Mockito.mock(AuditNotificationRepository.class);
        final CommonDistributionRepository commonDistributionRepository = Mockito.mock(CommonDistributionRepository.class);

        final NotificationContent notificationContent = new MockNotificationContent(new Date(), "provider", new Date(), "notificationType", "{content: \"content is here...\"}", 1L).createEntity();
        final MockCommonDistributionEntity mockCommonDistributionEntity = new MockCommonDistributionEntity();
        final ContentConverter contentConverter = new ContentConverter(new Gson(), new DefaultConversionService());
        final NotificationContentConverter notificationContentConverter = new NotificationContentConverter(contentConverter);
        Mockito.when(commonDistributionRepository.findAll()).thenReturn(Arrays.asList(mockCommonDistributionEntity.createEntity()));
        Mockito.when(notificationRepository.findAllById(Mockito.anyList())).thenReturn(Arrays.asList(notificationContent));
        final AuditEntryActions auditEntryActions = new AuditEntryActions(auditEntryRepository, new NotificationManager(notificationRepository, vulnerabilityRepository, auditEntryRepository, auditNotificationRepository),
            auditNotificationRepository, commonDistributionRepository, notificationContentConverter, null, null, null);

        final AlertPagedModel<AuditEntryConfig> restModel = auditEntryActions.get(currentPage, pageSize, null, null, null);
        assertEquals(pageResponse.getTotalPages(), restModel.getTotalPages());
        assertEquals(pageResponse.getNumber(), restModel.getCurrentPage());
        assertEquals(pageResponse.getSize(), restModel.getPageSize());

        for (int index = 0; index < pageSize; index++) {
            final AuditEntryEntity entity = pageResponse.getContent().get(index);
            final AuditEntryConfig entryRestModel = restModel.getContent().get(index);
            assertEquals(String.valueOf(entity.getId()), entryRestModel.getId());
        }
    }

    @Test
    public void testPagedRequestEmptyList() {
        final int totalPages = 1;
        final int currentPage = 1;
        final int pageSize = 1;
        @SuppressWarnings("unchecked") final Page<AuditEntryEntity> pageResponse = Mockito.mock(Page.class);

        Mockito.when(pageResponse.getContent()).thenReturn(Collections.emptyList());
        Mockito.when(pageResponse.getTotalPages()).thenReturn(totalPages);
        Mockito.when(pageResponse.getNumber()).thenReturn(currentPage);
        Mockito.when(pageResponse.getSize()).thenReturn(0);

        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        Mockito.when(auditEntryRepository.findAll(Mockito.any(PageRequest.class))).thenReturn(pageResponse);

        final NotificationContentRepository notificationRepository = Mockito.mock(NotificationContentRepository.class);
        final VulnerabilityRepository vulnerabilityRepository = Mockito.mock(VulnerabilityRepository.class);
        final AuditNotificationRepository auditNotificationRepository = Mockito.mock(AuditNotificationRepository.class);
        final CommonDistributionRepository commonDistributionRepository = Mockito.mock(CommonDistributionRepository.class);
        final MockCommonDistributionEntity mockCommonDistributionEntity = new MockCommonDistributionEntity();
        final ContentConverter contentConverter = new ContentConverter(new Gson(), new DefaultConversionService());
        final NotificationContentConverter notificationContentConverter = new NotificationContentConverter(contentConverter);
        final NotificationContent notificationContent = new MockNotificationContent(new Date(), "provider", new Date(), "notificationType", "{content: \"content is here...\"}", 1L).createEntity();
        Mockito.when(commonDistributionRepository.findAll()).thenReturn(Arrays.asList(mockCommonDistributionEntity.createEntity()));
        Mockito.when(notificationRepository.findAllById(Mockito.anyList())).thenReturn(Arrays.asList(notificationContent));
        final AuditEntryActions auditEntryActions = new AuditEntryActions(auditEntryRepository, new NotificationManager(notificationRepository, vulnerabilityRepository, auditEntryRepository, auditNotificationRepository),
            auditNotificationRepository, commonDistributionRepository, notificationContentConverter, null, null, null);

        final AlertPagedModel<AuditEntryConfig> restModel = auditEntryActions.get(currentPage, pageSize, null, null, null);
        assertEquals(pageResponse.getTotalPages(), restModel.getTotalPages());
        assertEquals(pageResponse.getNumber(), restModel.getCurrentPage());
        //Assert 0 because there aren't any entries in the pageResponse content
        assertEquals(0, restModel.getPageSize());
        assertTrue(restModel.getContent().isEmpty());
    }

}
