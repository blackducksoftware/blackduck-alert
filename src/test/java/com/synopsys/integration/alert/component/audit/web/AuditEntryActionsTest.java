package com.synopsys.integration.alert.component.audit.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryModel;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryPageModel;
import com.synopsys.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.component.audit.AuditDescriptorKey;
import com.synopsys.integration.alert.component.audit.mock.MockAuditEntryEntity;
import com.synopsys.integration.alert.database.api.DefaultNotificationAccessor;
import com.synopsys.integration.alert.database.api.DefaultRestApiAuditAccessor;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.notification.NotificationContentRepository;
import com.synopsys.integration.alert.database.notification.NotificationEntity;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.mock.entity.MockNotificationContent;
import com.synopsys.integration.alert.test.common.OutputLogger;

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
        Mockito.when(authorizationManager.hasReadPermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);
        AuditDescriptorKey auditDescriptorKey = new AuditDescriptorKey();
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        Mockito.when(auditEntryRepository.findFirstByCommonConfigIdOrderByTimeLastSentDesc(Mockito.any())).thenReturn(Optional.empty());

        DefaultNotificationAccessor notificationAccessor = Mockito.mock(DefaultNotificationAccessor.class);
        Mockito.when(notificationAccessor.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        DefaultRestApiAuditAccessor auditEntryUtility = new DefaultRestApiAuditAccessor(auditEntryRepository, null, null, null, notificationAccessor, null);
        AuditEntryActions auditEntryActions = new AuditEntryActions(authorizationManager, auditDescriptorKey, auditEntryUtility, notificationAccessor, null, null, null);

        ActionResponse<AuditEntryModel> auditEntryModel = auditEntryActions.get(1L);
        assertTrue(auditEntryModel.isError());
        assertFalse(auditEntryModel.hasContent());
    }

    @Test
    public void testGetAuditInfoForJobNull() {
        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.when(authorizationManager.hasReadPermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);
        AuditDescriptorKey auditDescriptorKey = new AuditDescriptorKey();
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        Mockito.when(auditEntryRepository.findFirstByCommonConfigIdOrderByTimeLastSentDesc(Mockito.any())).thenReturn(Optional.empty());

        DefaultRestApiAuditAccessor auditEntryUtility = new DefaultRestApiAuditAccessor(auditEntryRepository, null, null, null, null, null);
        AuditEntryActions auditEntryActions = new AuditEntryActions(authorizationManager, auditDescriptorKey, auditEntryUtility, null, null, null, null);

        ActionResponse<AuditJobStatusModel> jobAuditModel = auditEntryActions.getAuditInfoForJob(UUID.randomUUID());
        assertTrue(jobAuditModel.isError());
        assertFalse(jobAuditModel.hasContent());
    }

    @Test
    public void testResendNotificationException() {
        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.when(authorizationManager.hasExecutePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);
        AuditDescriptorKey auditDescriptorKey = new AuditDescriptorKey();
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        NotificationContentRepository notificationRepository = Mockito.mock(NotificationContentRepository.class);
        AuditNotificationRepository auditNotificationRepository = Mockito.mock(AuditNotificationRepository.class);
        JobAccessor jobAccessor = Mockito.mock(JobAccessor.class);
        Mockito.when(jobAccessor.getJobById(Mockito.any())).thenReturn(null);
        MockAuditEntryEntity mockAuditEntryEntity = new MockAuditEntryEntity();
        MockNotificationContent mockNotificationEntity = new MockNotificationContent();
        Mockito.when(auditEntryRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(mockAuditEntryEntity.createEmptyEntity()));
        Mockito.when(notificationRepository.findAllById(Mockito.anyList())).thenReturn(Collections.singletonList(mockNotificationEntity.createEntity()));

        DefaultNotificationAccessor notificationManager = new DefaultNotificationAccessor(notificationRepository, auditEntryRepository, null);
        DefaultRestApiAuditAccessor auditEntryUtility = new DefaultRestApiAuditAccessor(auditEntryRepository, auditNotificationRepository, jobAccessor, null, notificationManager, null);
        AuditEntryActions auditEntryActions = new AuditEntryActions(authorizationManager, auditDescriptorKey, auditEntryUtility, notificationManager, jobAccessor, null, null);

        ActionResponse<AuditEntryPageModel> response = auditEntryActions.resendNotification(1L, null);
        assertTrue(response.isError());
        assertFalse(response.hasContent());
    }

    @Test
    public void testPagedRequest() {
        int totalPages = 2;
        int currentPage = 0;
        int pageSize = 2;
        OffsetDateTime createdAt = DateUtils.createCurrentDateTimestamp();
        AlertNotificationModel entity_1 = new AlertNotificationModel(1L, 1L, "provider", "providerConfigName", "notificationType", "{content: \"content is here...\"}", createdAt, createdAt, false);
        entity_1.setId(1L);
        AlertNotificationModel entity_2 = new AlertNotificationModel(2L, 2L, "provider", "providerConfigName", "notificationType", "{content: \"content is here...\"}", createdAt, createdAt, false);
        entity_2.setId(2L);
        List<AlertNotificationModel> pagedEntryList = Arrays.asList(entity_1, entity_2);
        Page<AlertNotificationModel> pageResponse = Mockito.mock(Page.class);

        Mockito.when(pageResponse.getContent()).thenReturn(pagedEntryList);
        Mockito.when(pageResponse.getTotalPages()).thenReturn(totalPages);
        Mockito.when(pageResponse.getNumber()).thenReturn(currentPage);
        Mockito.when(pageResponse.getSize()).thenReturn(pageSize);

        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.when(authorizationManager.hasReadPermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);
        AuditDescriptorKey auditDescriptorKey = new AuditDescriptorKey();
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);

        DefaultNotificationAccessor notificationManager = Mockito.mock(DefaultNotificationAccessor.class);
        Mockito.when(notificationManager.findAll(Mockito.any(PageRequest.class), Mockito.anyBoolean())).thenReturn(pageResponse);
        PageRequest pageRequest = PageRequest.of(currentPage, pageSize, Sort.unsorted());
        Mockito.when(notificationManager.getPageRequestForNotifications(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(), Mockito.any())).thenReturn(pageRequest);

        NotificationContentRepository notificationRepository = Mockito.mock(NotificationContentRepository.class);
        AuditNotificationRepository auditNotificationRepository = Mockito.mock(AuditNotificationRepository.class);
        JobAccessor jobAccessor = Mockito.mock(JobAccessor.class);
        Mockito.when(jobAccessor.getJobById(Mockito.any())).thenReturn(null);
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);

        NotificationEntity notificationContent = new MockNotificationContent(DateUtils.createCurrentDateTimestamp(), "provider", DateUtils.createCurrentDateTimestamp(), "notificationType", "{content: \"content is here...\"}", 1L, 1L)
            .createEntity();
        ContentConverter contentConverter = new ContentConverter(new DefaultConversionService());

        DistributionJobModel distributionJob = DistributionJobModel.builder()
            .jobId(UUID.randomUUID()).enabled(true).blackDuckGlobalConfigId(2L).channelDescriptorName("distributionType").name("name").createdAt(OffsetDateTime.now())
            .distributionFrequency(FrequencyType.REAL_TIME).filterByProject(false).notificationTypes(List.of("type")).processingType(ProcessingType.DEFAULT).build();

        Mockito.doReturn(Optional.of(distributionJob)).when(jobAccessor).getJobById(Mockito.any());
        Mockito.when(notificationRepository.findAllById(Mockito.anyList())).thenReturn(Collections.singletonList(notificationContent));

        DefaultRestApiAuditAccessor auditEntryUtility = new DefaultRestApiAuditAccessor(auditEntryRepository, auditNotificationRepository, jobAccessor, configurationModelConfigurationAccessor, notificationManager, contentConverter);
        AuditEntryActions auditEntryActions = new AuditEntryActions(authorizationManager, auditDescriptorKey, auditEntryUtility, notificationManager, jobAccessor, null, null);

        ActionResponse<AuditEntryPageModel> response = auditEntryActions.get(currentPage, pageSize, null, null, null, true);

        assertTrue(response.hasContent());
        AuditEntryPageModel restModel = response.getContent().orElse(null);
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
    public void testPagedRequestEmptyList() {
        int totalPages = 1;
        int currentPage = 1;
        int pageSize = 1;
        Page<AlertNotificationModel> pageResponse = Mockito.mock(Page.class);

        Mockito.when(pageResponse.getContent()).thenReturn(Collections.emptyList());
        Mockito.when(pageResponse.getTotalPages()).thenReturn(totalPages);
        Mockito.when(pageResponse.getNumber()).thenReturn(currentPage);
        Mockito.when(pageResponse.getSize()).thenReturn(0);

        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.when(authorizationManager.hasReadPermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);
        AuditDescriptorKey auditDescriptorKey = new AuditDescriptorKey();
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);

        DefaultNotificationAccessor notificationManager = Mockito.mock(DefaultNotificationAccessor.class);
        Mockito.when(notificationManager.findAll(Mockito.any(PageRequest.class), Mockito.anyBoolean())).thenReturn(pageResponse);
        PageRequest pageRequest = PageRequest.of(currentPage, pageSize, Sort.unsorted());
        Mockito.when(notificationManager.getPageRequestForNotifications(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(), Mockito.any())).thenReturn(pageRequest);

        NotificationContentRepository notificationRepository = Mockito.mock(NotificationContentRepository.class);
        AuditNotificationRepository auditNotificationRepository = Mockito.mock(AuditNotificationRepository.class);
        JobAccessor jobAccessor = Mockito.mock(JobAccessor.class);
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);

        ContentConverter contentConverter = new ContentConverter(new DefaultConversionService());
        NotificationEntity notificationContent = new MockNotificationContent(DateUtils.createCurrentDateTimestamp(), "provider", DateUtils.createCurrentDateTimestamp(), "notificationType", "{content: \"content is here...\"}", 1L, 1L)
            .createEntity();
        DistributionJobModel distributionJob = DistributionJobModel.builder()
            .jobId(UUID.randomUUID()).enabled(true).blackDuckGlobalConfigId(2L).channelDescriptorName("distributionType").name("name").createdAt(OffsetDateTime.now())
            .distributionFrequency(FrequencyType.REAL_TIME).filterByProject(false).notificationTypes(List.of("type")).processingType(ProcessingType.DEFAULT).build();

        Mockito.doReturn(Optional.of(distributionJob)).when(jobAccessor).getJobById(Mockito.any());
        Mockito.when(notificationRepository.findAllById(Mockito.anyList())).thenReturn(Collections.singletonList(notificationContent));

        DefaultRestApiAuditAccessor auditEntryUtility = new DefaultRestApiAuditAccessor(auditEntryRepository, auditNotificationRepository, jobAccessor, configurationModelConfigurationAccessor, notificationManager, contentConverter);
        AuditEntryActions auditEntryActions = new AuditEntryActions(authorizationManager, auditDescriptorKey, auditEntryUtility, notificationManager, jobAccessor, null, null);

        ActionResponse<AuditEntryPageModel> response = auditEntryActions.get(currentPage, pageSize, null, null, null, true);

        assertTrue(response.hasContent());

        AuditEntryPageModel restModel = response.getContent().orElse(null);
        assertEquals(pageResponse.getTotalPages(), restModel.getTotalPages());
        assertEquals(pageResponse.getNumber(), restModel.getCurrentPage());
        //Assert 0 because there aren't any entries in the pageResponse content
        assertEquals(0, restModel.getPageSize());
        assertTrue(restModel.getContent().isEmpty());
    }

}
