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
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.google.gson.Gson;
import com.synopsys.integration.alert.audit.mock.MockAuditEntryEntity;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryModel;
import com.synopsys.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.database.api.AuditEntryUtility;
import com.synopsys.integration.alert.database.api.JobConfigReader;
import com.synopsys.integration.alert.database.api.NotificationManager;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.notification.NotificationContent;
import com.synopsys.integration.alert.database.notification.NotificationContentRepository;
import com.synopsys.integration.alert.mock.MockConfigurationModelFactory;
import com.synopsys.integration.alert.mock.entity.MockNotificationContent;
import com.synopsys.integration.alert.util.OutputLogger;
import com.synopsys.integration.alert.web.audit.AuditEntryActions;
import com.synopsys.integration.exception.IntegrationException;

public class AuditEntryActionsTest {
    private OutputLogger outputLogger;

    @BeforeEach
    public void init() throws IOException {
        outputLogger = new OutputLogger();
    }

    @AfterEach
    public void cleanup() throws IOException {
        outputLogger.cleanup();
    }

    @Test
    public void testGetNull() {
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        Mockito.when(auditEntryRepository.findFirstByCommonConfigIdOrderByTimeLastSentDesc(Mockito.any())).thenReturn(Optional.empty());

        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        Mockito.when(notificationManager.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        final AuditEntryUtility auditEntryUtility = new AuditEntryUtility(auditEntryRepository, null, null, notificationManager, null);
        final AuditEntryActions auditEntryActions = new AuditEntryActions(auditEntryUtility, notificationManager, null, null, null);

        final Optional<AuditEntryModel> auditEntryModel = auditEntryActions.get(1L);
        assertTrue(auditEntryModel.isEmpty());
    }

    @Test
    public void testGetAuditInfoForJobNull() {
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        Mockito.when(auditEntryRepository.findFirstByCommonConfigIdOrderByTimeLastSentDesc(Mockito.any())).thenReturn(Optional.empty());

        final AuditEntryUtility auditEntryUtility = new AuditEntryUtility(auditEntryRepository, null, null, null, null);
        final AuditEntryActions auditEntryActions = new AuditEntryActions(auditEntryUtility, null, null, null, null);

        final Optional<AuditJobStatusModel> jobAuditModel = auditEntryActions.getAuditInfoForJob(UUID.randomUUID());
        assertTrue(jobAuditModel.isEmpty());
    }

    @Test
    public void testResendNotificationException() {
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        final NotificationContentRepository notificationRepository = Mockito.mock(NotificationContentRepository.class);
        final AuditNotificationRepository auditNotificationRepository = Mockito.mock(AuditNotificationRepository.class);
        final JobConfigReader jobConfigReader = Mockito.mock(JobConfigReader.class);
        final MockAuditEntryEntity mockAuditEntryEntity = new MockAuditEntryEntity();
        final MockNotificationContent mockNotificationEntity = new MockNotificationContent();
        Mockito.when(auditEntryRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(mockAuditEntryEntity.createEmptyEntity()));
        Mockito.when(jobConfigReader.getPopulatedJobConfig(Mockito.any())).thenReturn(null);
        Mockito.when(notificationRepository.findAllById(Mockito.anyList())).thenReturn(Collections.singletonList(mockNotificationEntity.createEntity()));

        final NotificationManager notificationManager = new NotificationManager(notificationRepository, auditEntryRepository, auditNotificationRepository);
        final AuditEntryUtility auditEntryUtility = new AuditEntryUtility(auditEntryRepository, auditNotificationRepository, jobConfigReader, notificationManager, null);
        final AuditEntryActions auditEntryActions = new AuditEntryActions(auditEntryUtility, notificationManager, jobConfigReader, null, null);

        AlertPagedModel<AuditEntryModel> restModel = null;
        try {
            restModel = auditEntryActions.resendNotification(1L, null);
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

        final NotificationContent entity_1 = new NotificationContent();
        entity_1.setId(1L);
        final NotificationContent entity_2 = new NotificationContent();
        entity_2.setId(2L);
        final List<AlertNotificationWrapper> pagedEntryList = Arrays.asList(entity_1, entity_2);
        @SuppressWarnings("unchecked") final Page<AlertNotificationWrapper> pageResponse = Mockito.mock(Page.class);

        Mockito.when(pageResponse.getContent()).thenReturn(pagedEntryList);
        Mockito.when(pageResponse.getTotalPages()).thenReturn(totalPages);
        Mockito.when(pageResponse.getNumber()).thenReturn(currentPage);
        Mockito.when(pageResponse.getSize()).thenReturn(pageSize);

        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);

        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        Mockito.when(notificationManager.findAll(Mockito.any(PageRequest.class), Mockito.anyBoolean())).thenReturn(pageResponse);
        final PageRequest pageRequest = PageRequest.of(currentPage, pageSize, null);
        Mockito.when(notificationManager.getPageRequestForNotifications(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(), Mockito.any())).thenReturn(pageRequest);

        final NotificationContentRepository notificationRepository = Mockito.mock(NotificationContentRepository.class);
        final AuditNotificationRepository auditNotificationRepository = Mockito.mock(AuditNotificationRepository.class);
        final JobConfigReader jobConfigReader = Mockito.mock(JobConfigReader.class);

        final NotificationContent notificationContent = new MockNotificationContent(new Date(), "provider", new Date(), "notificationType", "{content: \"content is here...\"}", 1L).createEntity();
        final ContentConverter contentConverter = new ContentConverter(new Gson(), new DefaultConversionService());

        final ConfigurationModel configuration = MockConfigurationModelFactory.createCommonConfigModel(1L, 2L, "distributionType", "name", "providerName", "frequency",
            "filterByProject", "projectNamePattern", Collections.emptyList(), Collections.emptyList(), "formatType");

        Mockito.doReturn(Optional.of(configuration)).when(jobConfigReader).getPopulatedJobConfig(Mockito.any());
        Mockito.when(notificationRepository.findAllById(Mockito.anyList())).thenReturn(Collections.singletonList(notificationContent));

        final AuditEntryUtility auditEntryUtility = new AuditEntryUtility(auditEntryRepository, auditNotificationRepository, jobConfigReader, notificationManager, contentConverter);
        final AuditEntryActions auditEntryActions = new AuditEntryActions(auditEntryUtility, notificationManager, jobConfigReader, null, null);

        final AlertPagedModel<AuditEntryModel> restModel = auditEntryActions.get(currentPage, pageSize, null, null, null, true);
        assertEquals(pageResponse.getTotalPages(), restModel.getTotalPages());
        assertEquals(pageResponse.getNumber(), restModel.getCurrentPage());
        assertEquals(pageResponse.getSize(), restModel.getPageSize());

        for (int index = 0; index < pageSize; index++) {
            final AlertNotificationWrapper entity = pageResponse.getContent().get(index);
            final AuditEntryModel entryRestModel = restModel.getContent().get(index);
            assertEquals(String.valueOf(entity.getId()), entryRestModel.getId());
        }
    }

    @Test
    public void testPagedRequestEmptyList() {
        final int totalPages = 1;
        final int currentPage = 1;
        final int pageSize = 1;
        @SuppressWarnings("unchecked") final Page<AlertNotificationWrapper> pageResponse = Mockito.mock(Page.class);

        Mockito.when(pageResponse.getContent()).thenReturn(Collections.emptyList());
        Mockito.when(pageResponse.getTotalPages()).thenReturn(totalPages);
        Mockito.when(pageResponse.getNumber()).thenReturn(currentPage);
        Mockito.when(pageResponse.getSize()).thenReturn(0);

        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);

        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        Mockito.when(notificationManager.findAll(Mockito.any(PageRequest.class), Mockito.anyBoolean())).thenReturn(pageResponse);
        final PageRequest pageRequest = PageRequest.of(currentPage, pageSize, null);
        Mockito.when(notificationManager.getPageRequestForNotifications(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(), Mockito.any())).thenReturn(pageRequest);

        final NotificationContentRepository notificationRepository = Mockito.mock(NotificationContentRepository.class);
        final AuditNotificationRepository auditNotificationRepository = Mockito.mock(AuditNotificationRepository.class);
        final JobConfigReader jobConfigReader = Mockito.mock(JobConfigReader.class);
        final ContentConverter contentConverter = new ContentConverter(new Gson(), new DefaultConversionService());
        final NotificationContent notificationContent = new MockNotificationContent(new Date(), "provider", new Date(), "notificationType", "{content: \"content is here...\"}", 1L).createEntity();

        final ConfigurationModel configuration = MockConfigurationModelFactory.createCommonConfigModel(1L, 2L, "distributionType", "name", "providerName", "frequency",
            "filterByProject", "projectNamePattern", Collections.emptyList(), Collections.emptyList(), "formatType");

        Mockito.doReturn(Optional.of(configuration)).when(jobConfigReader).getPopulatedJobConfig(Mockito.any());
        Mockito.when(notificationRepository.findAllById(Mockito.anyList())).thenReturn(Collections.singletonList(notificationContent));

        final AuditEntryUtility auditEntryUtility = new AuditEntryUtility(auditEntryRepository, auditNotificationRepository, jobConfigReader, notificationManager, contentConverter);
        final AuditEntryActions auditEntryActions = new AuditEntryActions(auditEntryUtility, notificationManager, jobConfigReader, null, null);

        final AlertPagedModel<AuditEntryModel> restModel = auditEntryActions.get(currentPage, pageSize, null, null, null, true);
        assertEquals(pageResponse.getTotalPages(), restModel.getTotalPages());
        assertEquals(pageResponse.getNumber(), restModel.getCurrentPage());
        //Assert 0 because there aren't any entries in the pageResponse content
        assertEquals(0, restModel.getPageSize());
        assertTrue(restModel.getContent().isEmpty());
    }

}
