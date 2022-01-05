/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.audit.web;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.RestApiAuditAccessor;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryModel;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryPageModel;
import com.synopsys.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.AuditJobStatusesModel;
import com.synopsys.integration.alert.common.rest.model.JobIdsRequestModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.audit.AuditDescriptorKey;
import com.synopsys.integration.alert.processor.api.JobNotificationProcessor;
import com.synopsys.integration.alert.processor.api.NotificationProcessor;
import com.synopsys.integration.alert.processor.api.distribute.ProcessedNotificationDetails;

@Component
@Transactional
public class AuditEntryActions {
    private final Logger logger = LoggerFactory.getLogger(AuditEntryActions.class);

    private final AuthorizationManager authorizationManager;
    private final AuditDescriptorKey descriptorKey;
    private final NotificationAccessor notificationAccessor;
    private final RestApiAuditAccessor auditAccessor;
    private final JobAccessor jobAccessor;
    private final NotificationProcessor notificationProcessor;
    private final JobNotificationProcessor jobNotificationProcessor;

    @Autowired
    public AuditEntryActions(
        AuthorizationManager authorizationManager,
        AuditDescriptorKey descriptorKey,
        RestApiAuditAccessor auditAccessor,
        NotificationAccessor notificationAccessor,
        JobAccessor jobAccessor,
        NotificationProcessor notificationProcessor,
        JobNotificationProcessor jobNotificationProcessor
    ) {
        this.authorizationManager = authorizationManager;
        this.descriptorKey = descriptorKey;
        this.auditAccessor = auditAccessor;
        this.notificationAccessor = notificationAccessor;
        this.jobAccessor = jobAccessor;
        this.notificationProcessor = notificationProcessor;
        this.jobNotificationProcessor = jobNotificationProcessor;
    }

    public ActionResponse<AuditEntryPageModel> get() {
        return get(null, null, null, null, null, false);
    }

    public ActionResponse<AuditEntryPageModel> get(Integer pageNumber, Integer pageSize, String searchTerm, String sortField, String sortOrder, boolean onlyShowSentNotifications) {
        if (!authorizationManager.hasReadPermission(ConfigContextEnum.GLOBAL, descriptorKey)) {
            return new ActionResponse<>(HttpStatus.FORBIDDEN, ActionResponse.FORBIDDEN_MESSAGE);
        }
        Integer page = ObjectUtils.defaultIfNull(pageNumber, AlertPagedModel.DEFAULT_PAGE_NUMBER);
        Integer size = ObjectUtils.defaultIfNull(pageSize, AlertPagedModel.DEFAULT_PAGE_SIZE);
        AuditEntryPageModel pagedRestModel = auditAccessor.getPageOfAuditEntries(page, size, searchTerm, sortField, sortOrder, onlyShowSentNotifications, auditAccessor::convertToAuditEntryModelFromNotification);
        logger.debug("Paged Audit Entry Rest Model: {}", pagedRestModel);
        return new ActionResponse<>(HttpStatus.OK, pagedRestModel);
    }

    public ActionResponse<AuditEntryModel> get(Long id) {
        if (!authorizationManager.hasReadPermission(ConfigContextEnum.GLOBAL, descriptorKey)) {
            return new ActionResponse<>(HttpStatus.FORBIDDEN, ActionResponse.FORBIDDEN_MESSAGE);
        }
        Optional<AlertNotificationModel> notificationContent = notificationAccessor.findById(id);
        if (notificationContent.isPresent()) {
            AuditEntryModel auditEntryModel = auditAccessor.convertToAuditEntryModelFromNotification(notificationContent.get());
            return new ActionResponse<>(HttpStatus.OK, auditEntryModel);
        }
        return new ActionResponse<>(HttpStatus.GONE, "This Audit entry could not be found.");
    }

    public ActionResponse<AuditJobStatusModel> getAuditInfoForJob(UUID jobId) {
        if (!authorizationManager.hasReadPermission(ConfigContextEnum.GLOBAL, descriptorKey)) {
            return new ActionResponse<>(HttpStatus.FORBIDDEN, ActionResponse.FORBIDDEN_MESSAGE);
        }
        Optional<AuditJobStatusModel> auditJobStatusModel = auditAccessor.findFirstByJobId(jobId);
        if (auditJobStatusModel.isPresent()) {
            return new ActionResponse<>(HttpStatus.OK, auditJobStatusModel.get());
        }
        return new ActionResponse<>(HttpStatus.GONE, "The Audit information could not be found for this job.");
    }

    public ActionResponse<AuditJobStatusesModel> queryForAuditInfoInJobs(JobIdsRequestModel queryRequestModel) {
        if (!authorizationManager.hasReadPermission(ConfigContextEnum.GLOBAL, descriptorKey)) {
            return new ActionResponse<>(HttpStatus.FORBIDDEN, ActionResponse.FORBIDDEN_MESSAGE);
        }

        List<UUID> jobIds = queryRequestModel.getJobIds();
        for (UUID jobId : jobIds) {
            if (null == jobId) {
                return new ActionResponse<>(HttpStatus.BAD_REQUEST, "The field 'jobIds' cannot contain null values");
            }
        }

        List<AuditJobStatusModel> auditJobStatusModels = auditAccessor.findByJobIds(jobIds);
        return new ActionResponse<>(HttpStatus.OK, new AuditJobStatusesModel(auditJobStatusModels));
    }

    public ActionResponse<AuditEntryPageModel> resendNotification(Long notificationId, @Nullable UUID requestedJobId) {
        if (!authorizationManager.hasExecutePermission(ConfigContextEnum.GLOBAL, descriptorKey)) {
            return new ActionResponse<>(HttpStatus.FORBIDDEN, ActionResponse.FORBIDDEN_MESSAGE);
        }

        Optional<AlertNotificationModel> notification = notificationAccessor
            .findById(notificationId);
        if (notification.isEmpty()) {
            return new ActionResponse<>(HttpStatus.GONE, "No notification with this id exists.");
        }
        AlertNotificationModel notificationContent = notification.get();

        if (null != requestedJobId) {
            Optional<DistributionJobModel> optionalDistributionJob = jobAccessor.getJobById(requestedJobId);
            if (optionalDistributionJob.isEmpty()) {
                String message = String.format("The Distribution Job with this id could not be found. %s", requestedJobId.toString());
                return new ActionResponse<>(HttpStatus.GONE, message);
            }
            DistributionJobModel distributionJob = optionalDistributionJob.get();
            if (distributionJob.isEnabled()) {
                ProcessedNotificationDetails processedNotificationDetails = ProcessedNotificationDetails.fromDistributionJob(distributionJob);
                jobNotificationProcessor.processNotificationForJob(
                    processedNotificationDetails,
                    distributionJob.getProcessingType(),
                    List.of(notificationContent)
                );
            } else {
                UUID jobConfigId = distributionJob.getJobId();
                logger.warn("The Distribution Job with Id {} was disabled. This notification could not be sent", jobConfigId);
                String message = String.format("The Distribution Job is currently disabled. %s", jobConfigId);
                return new ActionResponse<>(HttpStatus.BAD_REQUEST, message);
            }
        } else {
            notificationProcessor.processNotifications(List.of(notificationContent), List.of(FrequencyType.DAILY, FrequencyType.REAL_TIME));
        }
        return get();
    }

}
