package com.synopsys.integration.alert.database.api;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingFailedAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.database.audit.AuditFailedEntity;
import com.synopsys.integration.alert.database.audit.AuditFailedEntryRepository;

@Component
public class DefaultProcessingFailedAccessor implements ProcessingFailedAccessor {

    public static final String UNKNOWN_JOB = "Job Unknown";
    public static final String UNKNOWN_CHANNEL = "Unknown Channel";
    private final AuditFailedEntryRepository auditFailedEntryRepository;
    private final NotificationAccessor notificationAccessor;
    private final JobAccessor jobAccessor;

    @Autowired
    public DefaultProcessingFailedAccessor(
        AuditFailedEntryRepository auditFailedEntryRepository,
        NotificationAccessor notificationAccessor,
        JobAccessor jobAccessor
    ) {
        this.auditFailedEntryRepository = auditFailedEntryRepository;
        this.notificationAccessor = notificationAccessor;
        this.jobAccessor = jobAccessor;
    }

    @Override
    @Transactional
    public void setAuditFailure(UUID jobId, Set<Long> notificationIds, OffsetDateTime occurrence, String errorMessage) {
        List<AlertNotificationModel> notificationModels = notificationAccessor.findByIds(new ArrayList<>(notificationIds));
        Optional<DistributionJobModel> distributionJobModel = jobAccessor.getJobById(jobId);
        for (AlertNotificationModel notificationModel : notificationModels) {
            AuditFailedEntity auditFailedEntity = new AuditFailedEntity(
                UUID.randomUUID(),
                occurrence,
                distributionJobModel.map(DistributionJobModel::getName).orElse(UNKNOWN_JOB),
                notificationModel.getProviderConfigName(),
                distributionJobModel.map(DistributionJobModel::getChannelDescriptorName).orElse(UNKNOWN_CHANNEL),
                notificationModel.getNotificationType(),
                errorMessage,
                notificationModel.getContent()
            );
            auditFailedEntryRepository.save(auditFailedEntity);
        }
    }

    @Override
    @Transactional
    public void setAuditFailure(UUID jobId, Set<Long> notificationIds, OffsetDateTime occurrence, String errorMessage, String stackTrace) {
        List<AlertNotificationModel> notificationModels = notificationAccessor.findByIds(new ArrayList<>(notificationIds));
        Optional<DistributionJobModel> distributionJobModel = jobAccessor.getJobById(jobId);
        for (AlertNotificationModel notificationModel : notificationModels) {
            AuditFailedEntity auditFailedEntity = new AuditFailedEntity(
                UUID.randomUUID(),
                occurrence,
                distributionJobModel.map(DistributionJobModel::getName).orElse(UNKNOWN_JOB),
                notificationModel.getProviderConfigName(),
                distributionJobModel.map(DistributionJobModel::getChannelDescriptorName).orElse(UNKNOWN_CHANNEL),
                notificationModel.getNotificationType(),
                errorMessage,
                stackTrace,
                notificationModel.getContent()
            );
            auditFailedEntryRepository.save(auditFailedEntity);
        }
    }
}
