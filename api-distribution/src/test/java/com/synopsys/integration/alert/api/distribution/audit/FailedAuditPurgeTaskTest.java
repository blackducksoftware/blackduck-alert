package com.synopsys.integration.alert.api.distribution.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.distribution.mock.MockAuditFailedEntryRepository;
import com.synopsys.integration.alert.api.distribution.mock.MockAuditFailedNotificationRepository;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.api.DefaultProcessingFailedAccessor;
import com.synopsys.integration.alert.database.audit.AuditFailedEntity;
import com.synopsys.integration.alert.database.audit.AuditFailedNotificationEntity;

class FailedAuditPurgeTaskTest {

    @Test
    void cronExpressionTest() {
        ConfigurationModelConfigurationAccessor configurationAccessor = createConfigurationAccessor(-1);
        FailedAuditPurgeTask task = new FailedAuditPurgeTask(null, null, null, configurationAccessor);
        assertEquals(String.format(FailedAuditPurgeTask.CRON_EXPRESSION_FORMAT, FailedAuditPurgeTask.DEFAULT_FREQUENCY), task.scheduleCronExpression());
    }

    @Test
    void purgeDataTest() {
        ConfigurationModelConfigurationAccessor configurationAccessor = createConfigurationAccessor(11);
        MockAuditFailedEntryRepository auditFailedEntryRepository = new MockAuditFailedEntryRepository(AuditFailedEntity::getId);
        MockAuditFailedNotificationRepository auditFailedNotificationRepository = new MockAuditFailedNotificationRepository(AuditFailedNotificationEntity::getNotificationId);
        NotificationAccessor notificationAccessor = Mockito.mock(NotificationAccessor.class);
        JobAccessor jobAccessor = Mockito.mock(JobAccessor.class);
        DefaultProcessingFailedAccessor processingFailedAccessor = new DefaultProcessingFailedAccessor(
            auditFailedEntryRepository,
            auditFailedNotificationRepository,
            notificationAccessor,
            jobAccessor
        );

        UUID expectedId = UUID.randomUUID();
        UUID firstIdToBeRemoved = UUID.randomUUID();
        UUID secondIdToBeRemoved = UUID.randomUUID();
        Long expectedNotificationId = 1L;
        auditFailedEntryRepository.save(new AuditFailedEntity(
            expectedId,
            DateUtils.createCurrentDateTimestamp(),
            "jobName",
            "providerKey",
            "providerName",
            "channelName",
            "notificationType",
            "errorMessage",
            expectedNotificationId
        ));
        auditFailedEntryRepository.save(new AuditFailedEntity(
            firstIdToBeRemoved,
            DateUtils.createCurrentDateTimestamp().minusDays(12),
            "jobName",
            "providerKey",
            "providerName",
            "channelName",
            "notificationType",
            "errorMessage",
            2L
        ));

        auditFailedEntryRepository.save(new AuditFailedEntity(
            secondIdToBeRemoved,
            DateUtils.createCurrentDateTimestamp().minusDays(25),
            "jobName",
            "providerKey",
            "providerName",
            "channelName",
            "notificationType",
            "errorMessage",
            3L
        ));

        auditFailedNotificationRepository.save(new AuditFailedNotificationEntity(1L, "notification 1 content"));
        auditFailedNotificationRepository.save(new AuditFailedNotificationEntity(2L, "notification 2 content"));
        auditFailedNotificationRepository.save(new AuditFailedNotificationEntity(3L, "notification 3 content"));

        FailedAuditPurgeTask task = new FailedAuditPurgeTask(null, null, processingFailedAccessor, configurationAccessor);
        task.runTask();
        assertEquals(1, auditFailedEntryRepository.count());
        assertEquals(1, auditFailedNotificationRepository.count());
        assertTrue(auditFailedEntryRepository.existsById(expectedId));
        assertFalse(auditFailedEntryRepository.existsById(firstIdToBeRemoved));
        assertFalse(auditFailedEntryRepository.existsById(secondIdToBeRemoved));
        assertTrue(auditFailedNotificationRepository.existsById(expectedNotificationId));
        assertFalse(auditFailedNotificationRepository.existsById(2L));
        assertFalse(auditFailedNotificationRepository.existsById(3L));
    }

    @Test
    void purgeDataMultipleNotificationsDifferentDatesTest() {
        ConfigurationModelConfigurationAccessor configurationAccessor = createConfigurationAccessor(11);
        MockAuditFailedEntryRepository auditFailedEntryRepository = new MockAuditFailedEntryRepository(AuditFailedEntity::getId);
        MockAuditFailedNotificationRepository auditFailedNotificationRepository = new MockAuditFailedNotificationRepository(AuditFailedNotificationEntity::getNotificationId);
        NotificationAccessor notificationAccessor = Mockito.mock(NotificationAccessor.class);
        JobAccessor jobAccessor = Mockito.mock(JobAccessor.class);
        DefaultProcessingFailedAccessor processingFailedAccessor = new DefaultProcessingFailedAccessor(
            auditFailedEntryRepository,
            auditFailedNotificationRepository,
            notificationAccessor,
            jobAccessor
        );

        UUID expectedId = UUID.randomUUID();
        UUID secondExpectedId = UUID.randomUUID();
        UUID firstIdToBeRemoved = UUID.randomUUID();
        UUID secondIdToBeRemoved = UUID.randomUUID();
        Long expectedNotificationId = 1L;
        auditFailedEntryRepository.save(new AuditFailedEntity(
            expectedId,
            DateUtils.createCurrentDateTimestamp(),
            "jobName",
            "providerKey",
            "providerName",
            "channelName",
            "notificationType",
            "errorMessage",
            expectedNotificationId
        ));
        auditFailedEntryRepository.save(new AuditFailedEntity(
            secondExpectedId,
            DateUtils.createCurrentDateTimestamp().minusDays(10),
            "jobName",
            "providerKey",
            "providerName",
            "channelName",
            "notificationType",
            "errorMessage",
            2L
        ));

        auditFailedEntryRepository.save(new AuditFailedEntity(
            firstIdToBeRemoved,
            DateUtils.createCurrentDateTimestamp().minusDays(12),
            "jobName",
            "providerKey",
            "providerName",
            "channelName",
            "notificationType",
            "errorMessage",
            2L
        ));

        auditFailedEntryRepository.save(new AuditFailedEntity(
            secondIdToBeRemoved,
            DateUtils.createCurrentDateTimestamp().minusDays(25),
            "jobName",
            "providerKey",
            "providerName",
            "channelName",
            "notificationType",
            "errorMessage",
            3L
        ));

        auditFailedNotificationRepository.save(new AuditFailedNotificationEntity(1L, "notification 1 content"));
        auditFailedNotificationRepository.save(new AuditFailedNotificationEntity(2L, "notification 2 content"));
        auditFailedNotificationRepository.save(new AuditFailedNotificationEntity(3L, "notification 3 content"));

        FailedAuditPurgeTask task = new FailedAuditPurgeTask(null, null, processingFailedAccessor, configurationAccessor);
        task.runTask();
        assertEquals(2, auditFailedEntryRepository.count());
        assertEquals(2, auditFailedNotificationRepository.count());
        assertTrue(auditFailedEntryRepository.existsById(expectedId));
        assertTrue(auditFailedEntryRepository.existsById(secondExpectedId));
        assertFalse(auditFailedEntryRepository.existsById(firstIdToBeRemoved));
        assertFalse(auditFailedEntryRepository.existsById(secondIdToBeRemoved));
        assertTrue(auditFailedNotificationRepository.existsById(expectedNotificationId));
        assertTrue(auditFailedNotificationRepository.existsById(2L));
        assertFalse(auditFailedNotificationRepository.existsById(3L));
    }

    private ConfigurationModelConfigurationAccessor createConfigurationAccessor(int purgeFrequencyDays) {
        ConfigurationModelConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);

        if (purgeFrequencyDays > 0) {
            Long registeredDescriptorId = 1L;
            Long descriptorConfigId = 1L;
            String createdAt = DateUtils.formatDateAsJsonString(DateUtils.createCurrentDateTimestamp());
            String lastUpdated = DateUtils.formatDateAsJsonString(DateUtils.createCurrentDateTimestamp());
            ConfigContextEnum context = ConfigContextEnum.GLOBAL;
            Map<String, ConfigurationFieldModel> fields = new HashMap<>();
            ConfigurationFieldModel fieldModel = ConfigurationFieldModel.create(FailedAuditPurgeTask.KEY_PURGE_AUDIT_FAILED_FREQUENCY_DAYS);
            fieldModel.setFieldValue(String.valueOf(purgeFrequencyDays));
            fields.put(FailedAuditPurgeTask.KEY_PURGE_AUDIT_FAILED_FREQUENCY_DAYS, fieldModel);
            ConfigurationModel configurationModel = new ConfigurationModel(registeredDescriptorId, descriptorConfigId, createdAt, lastUpdated, context, fields);
            List<ConfigurationModel> configurationModelList = List.of(configurationModel);
            Mockito.when(configurationAccessor.getConfigurationsByDescriptorNameAndContext(FailedAuditPurgeTask.DESCRIPTOR_NAME, ConfigContextEnum.GLOBAL))
                .thenReturn(configurationModelList);
        }

        return configurationAccessor;
    }
}
