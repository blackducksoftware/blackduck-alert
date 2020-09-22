package com.synopsys.integration.alert.web.api.audit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
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
import org.springframework.data.domain.Sort;

import com.google.gson.Gson;
import com.synopsys.integration.alert.audit.mock.MockAuditEntryEntity;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.channel.ChannelEventManager;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryModel;
import com.synopsys.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.component.audit.AuditDescriptorKey;
import com.synopsys.integration.alert.database.api.DefaultAuditUtility;
import com.synopsys.integration.alert.database.api.DefaultNotificationManager;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.notification.NotificationContentRepository;
import com.synopsys.integration.alert.database.notification.NotificationEntity;
import com.synopsys.integration.alert.mock.MockConfigurationModelFactory;
import com.synopsys.integration.alert.mock.entity.MockNotificationContent;
import com.synopsys.integration.alert.util.OutputLogger;

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
        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.when(authorizationManager.hasReadPermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        AuditDescriptorKey auditDescriptorKey = new AuditDescriptorKey();
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        Mockito.when(auditEntryRepository.findFirstByCommonConfigIdOrderByTimeLastSentDesc(Mockito.any())).thenReturn(Optional.empty());

        DefaultNotificationManager notificationManager = Mockito.mock(DefaultNotificationManager.class);
        Mockito.when(notificationManager.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        DefaultAuditUtility auditEntryUtility = new DefaultAuditUtility(auditEntryRepository, null, null, notificationManager, null);
        AuditEntryActions auditEntryActions = new AuditEntryActions(authorizationManager, auditDescriptorKey, auditEntryUtility, notificationManager, null, null, null);

        ActionResponse<AuditEntryModel> auditEntryModel = auditEntryActions.get(1L);
        assertTrue(auditEntryModel.isError());
        assertFalse(auditEntryModel.hasContent());
    }

    @Test
    public void testGetAuditInfoForJobNull() {
        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.when(authorizationManager.hasReadPermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        AuditDescriptorKey auditDescriptorKey = new AuditDescriptorKey();
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        Mockito.when(auditEntryRepository.findFirstByCommonConfigIdOrderByTimeLastSentDesc(Mockito.any())).thenReturn(Optional.empty());

        DefaultAuditUtility auditEntryUtility = new DefaultAuditUtility(auditEntryRepository, null, null, null, null);
        AuditEntryActions auditEntryActions = new AuditEntryActions(authorizationManager, auditDescriptorKey, auditEntryUtility, null, null, null, null);

        ActionResponse<AuditJobStatusModel> jobAuditModel = auditEntryActions.getAuditInfoForJob(UUID.randomUUID());
        assertTrue(jobAuditModel.isError());
        assertFalse(jobAuditModel.hasContent());
    }

    @Test
    public void testResendNotificationException() throws AlertDatabaseConstraintException {
        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.when(authorizationManager.hasExecutePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        AuditDescriptorKey auditDescriptorKey = new AuditDescriptorKey();
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        NotificationContentRepository notificationRepository = Mockito.mock(NotificationContentRepository.class);
        AuditNotificationRepository auditNotificationRepository = Mockito.mock(AuditNotificationRepository.class);
        ChannelEventManager eventManager = Mockito.mock(ChannelEventManager.class);
        ConfigurationAccessor jobConfigReader = Mockito.mock(ConfigurationAccessor.class);
        MockAuditEntryEntity mockAuditEntryEntity = new MockAuditEntryEntity();
        MockNotificationContent mockNotificationEntity = new MockNotificationContent();
        Mockito.when(auditEntryRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(mockAuditEntryEntity.createEmptyEntity()));
        Mockito.when(jobConfigReader.getJobById(Mockito.any())).thenReturn(null);
        Mockito.when(notificationRepository.findAllById(Mockito.anyList())).thenReturn(Collections.singletonList(mockNotificationEntity.createEntity()));

        DefaultNotificationManager notificationManager = new DefaultNotificationManager(notificationRepository, auditEntryRepository, auditNotificationRepository, jobConfigReader, eventManager);
        DefaultAuditUtility auditEntryUtility = new DefaultAuditUtility(auditEntryRepository, auditNotificationRepository, jobConfigReader, notificationManager, null);
        AuditEntryActions auditEntryActions = new AuditEntryActions(authorizationManager, auditDescriptorKey, auditEntryUtility, notificationManager, jobConfigReader, null, null);

        ActionResponse<AlertPagedModel<AuditEntryModel>> response = auditEntryActions.resendNotification(1L, null);
        assertTrue(response.isError());
        assertFalse(response.hasContent());
    }

    @Test
    public void testPagedRequest() throws AlertDatabaseConstraintException {
        int totalPages = 2;
        int currentPage = 0;
        int pageSize = 2;
        OffsetDateTime createdAt = DateUtils.createCurrentDateTimestamp();
        AlertNotificationModel entity_1 = new AlertNotificationModel(1L, 1L, "provider", "providerConfigName", "notificationType", "{content: \"content is here...\"}", createdAt, createdAt);
        entity_1.setId(1L);
        AlertNotificationModel entity_2 = new AlertNotificationModel(2L, 2L, "provider", "providerConfigName", "notificationType", "{content: \"content is here...\"}", createdAt, createdAt);
        entity_2.setId(2L);
        List<AlertNotificationModel> pagedEntryList = Arrays.asList(entity_1, entity_2);
        Page<AlertNotificationModel> pageResponse = Mockito.mock(Page.class);

        Mockito.when(pageResponse.getContent()).thenReturn(pagedEntryList);
        Mockito.when(pageResponse.getTotalPages()).thenReturn(totalPages);
        Mockito.when(pageResponse.getNumber()).thenReturn(currentPage);
        Mockito.when(pageResponse.getSize()).thenReturn(pageSize);

        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.when(authorizationManager.hasReadPermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        AuditDescriptorKey auditDescriptorKey = new AuditDescriptorKey();
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);

        DefaultNotificationManager notificationManager = Mockito.mock(DefaultNotificationManager.class);
        Mockito.when(notificationManager.findAll(Mockito.any(PageRequest.class), Mockito.anyBoolean())).thenReturn(pageResponse);
        PageRequest pageRequest = PageRequest.of(currentPage, pageSize, Sort.unsorted());
        Mockito.when(notificationManager.getPageRequestForNotifications(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(), Mockito.any())).thenReturn(pageRequest);

        NotificationContentRepository notificationRepository = Mockito.mock(NotificationContentRepository.class);
        AuditNotificationRepository auditNotificationRepository = Mockito.mock(AuditNotificationRepository.class);
        ConfigurationAccessor jobConfigReader = Mockito.mock(ConfigurationAccessor.class);

        NotificationEntity notificationContent = new MockNotificationContent(DateUtils.createCurrentDateTimestamp(), "provider", DateUtils.createCurrentDateTimestamp(), "notificationType", "{content: \"content is here...\"}", 1L, 1L)
                                                     .createEntity();
        ContentConverter contentConverter = new ContentConverter(new Gson(), new DefaultConversionService());

        ConfigurationModel configuration = MockConfigurationModelFactory.createCommonConfigModel(1L, 2L, "distributionType", "name", "providerName", "frequency",
            "filterByProject", "projectNamePattern", Collections.emptyList(), Collections.emptyList(), "formatType");

        Mockito.doReturn(Optional.of(configuration)).when(jobConfigReader).getJobById(Mockito.any());
        Mockito.when(notificationRepository.findAllById(Mockito.anyList())).thenReturn(Collections.singletonList(notificationContent));

        DefaultAuditUtility auditEntryUtility = new DefaultAuditUtility(auditEntryRepository, auditNotificationRepository, jobConfigReader, notificationManager, contentConverter);
        AuditEntryActions auditEntryActions = new AuditEntryActions(authorizationManager, auditDescriptorKey, auditEntryUtility, notificationManager, jobConfigReader, null, null);

        ActionResponse<AlertPagedModel<AuditEntryModel>> response = auditEntryActions.get(currentPage, pageSize, null, null, null, true);

        assertTrue(response.hasContent());
        AlertPagedModel<AuditEntryModel> restModel = response.getContent().orElse(null);
        assertEquals(pageResponse.getTotalPages(), restModel.getTotalPages());
        assertEquals(pageResponse.getNumber(), restModel.getCurrentPage());
        assertEquals(pageResponse.getSize(), restModel.getPageSize());

        for (int index = 0; index < pageSize; index++) {
            AlertNotificationModel entity = pageResponse.getContent().get(index);
            AuditEntryModel entryRestModel = restModel.getContent().get(index);
            assertEquals(String.valueOf(entity.getId()), entryRestModel.getId());
        }
    }

    @Test
    public void testPagedRequestEmptyList() throws AlertDatabaseConstraintException {
        int totalPages = 1;
        int currentPage = 1;
        int pageSize = 1;
        Page<AlertNotificationModel> pageResponse = Mockito.mock(Page.class);

        Mockito.when(pageResponse.getContent()).thenReturn(Collections.emptyList());
        Mockito.when(pageResponse.getTotalPages()).thenReturn(totalPages);
        Mockito.when(pageResponse.getNumber()).thenReturn(currentPage);
        Mockito.when(pageResponse.getSize()).thenReturn(0);

        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.when(authorizationManager.hasReadPermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        AuditDescriptorKey auditDescriptorKey = new AuditDescriptorKey();
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);

        DefaultNotificationManager notificationManager = Mockito.mock(DefaultNotificationManager.class);
        Mockito.when(notificationManager.findAll(Mockito.any(PageRequest.class), Mockito.anyBoolean())).thenReturn(pageResponse);
        PageRequest pageRequest = PageRequest.of(currentPage, pageSize, Sort.unsorted());
        Mockito.when(notificationManager.getPageRequestForNotifications(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(), Mockito.any())).thenReturn(pageRequest);

        NotificationContentRepository notificationRepository = Mockito.mock(NotificationContentRepository.class);
        AuditNotificationRepository auditNotificationRepository = Mockito.mock(AuditNotificationRepository.class);
        ConfigurationAccessor jobConfigReader = Mockito.mock(ConfigurationAccessor.class);
        ContentConverter contentConverter = new ContentConverter(new Gson(), new DefaultConversionService());
        NotificationEntity notificationContent = new MockNotificationContent(DateUtils.createCurrentDateTimestamp(), "provider", DateUtils.createCurrentDateTimestamp(), "notificationType", "{content: \"content is here...\"}", 1L, 1L)
                                                     .createEntity();

        ConfigurationModel configuration = MockConfigurationModelFactory.createCommonConfigModel(1L, 2L, "distributionType", "name", "providerName", "frequency",
            "filterByProject", "projectNamePattern", Collections.emptyList(), Collections.emptyList(), "formatType");

        Mockito.doReturn(Optional.of(configuration)).when(jobConfigReader).getJobById(Mockito.any());
        Mockito.when(notificationRepository.findAllById(Mockito.anyList())).thenReturn(Collections.singletonList(notificationContent));

        DefaultAuditUtility auditEntryUtility = new DefaultAuditUtility(auditEntryRepository, auditNotificationRepository, jobConfigReader, notificationManager, contentConverter);
        AuditEntryActions auditEntryActions = new AuditEntryActions(authorizationManager, auditDescriptorKey, auditEntryUtility, notificationManager, jobConfigReader, null, null);

        ActionResponse<AlertPagedModel<AuditEntryModel>> response = auditEntryActions.get(currentPage, pageSize, null, null, null, true);

        assertTrue(response.hasContent());

        AlertPagedModel<AuditEntryModel> restModel = response.getContent().orElse(null);
        assertEquals(pageResponse.getTotalPages(), restModel.getTotalPages());
        assertEquals(pageResponse.getNumber(), restModel.getCurrentPage());
        //Assert 0 because there aren't any entries in the pageResponse content
        assertEquals(0, restModel.getPageSize());
        assertTrue(restModel.getContent().isEmpty());
    }
}
