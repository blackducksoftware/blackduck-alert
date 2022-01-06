/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model.job;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.api.common.model.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;

public class DistributionJobModelBuilder {
    private static final String NOTIFICATION_TYPES_FIELD_NAME = "notificationTypes";

    private UUID jobId;
    private boolean enabled = true;
    private String name;
    private FrequencyType distributionFrequency;
    private ProcessingType processingType;
    private String channelDescriptorName;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastUpdated;

    private Long blackDuckGlobalConfigId;
    private boolean filterByProject = false;
    private String projectNamePattern;
    private String projectVersionNamePattern;
    private List<String> notificationTypes;
    private List<BlackDuckProjectDetailsModel> projectFilterDetails = List.of();
    private List<String> policyFilterPolicyNames = List.of();
    private List<String> vulnerabilityFilterSeverityNames = List.of();

    private DistributionJobDetailsModel distributionJobDetails;

    public DistributionJobModel build() {
        throwExceptionIfBlank(name, "name");
        throwExceptionIfNull(distributionFrequency, "distributionFrequency");
        throwExceptionIfNull(processingType, "processingType");
        throwExceptionIfBlank(channelDescriptorName, "channelDescriptorName");
        throwExceptionIfNull(createdAt, "createdAt");
        throwExceptionIfNull(blackDuckGlobalConfigId, "blackDuckGlobalConfigId");

        throwExceptionIfNull(notificationTypes, NOTIFICATION_TYPES_FIELD_NAME);
        if (notificationTypes.isEmpty()) {
            throw createMissingFieldException(NOTIFICATION_TYPES_FIELD_NAME);
        }

        if (filterByProject && StringUtils.isBlank(projectNamePattern) && StringUtils.isBlank(projectVersionNamePattern) && projectFilterDetails.isEmpty()) {
            throw new AlertRuntimeException("Missing project details");
        }

        return new DistributionJobModel(
            jobId,
            enabled,
            name,
            distributionFrequency,
            processingType,
            channelDescriptorName,
            createdAt,
            lastUpdated,
            blackDuckGlobalConfigId,
            filterByProject,
            projectNamePattern,
            projectVersionNamePattern,
            notificationTypes,
            projectFilterDetails,
            policyFilterPolicyNames,
            vulnerabilityFilterSeverityNames,
            distributionJobDetails
        );
    }

    public DistributionJobModelBuilder jobId(UUID jobId) {
        this.jobId = jobId;
        return this;
    }

    public DistributionJobModelBuilder enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public DistributionJobModelBuilder name(String name) {
        this.name = name;
        return this;
    }

    public DistributionJobModelBuilder distributionFrequency(String distributionFrequency) {
        this.distributionFrequency = EnumUtils.getEnum(FrequencyType.class, distributionFrequency);
        return this;
    }

    public DistributionJobModelBuilder distributionFrequency(FrequencyType distributionFrequency) {
        this.distributionFrequency = distributionFrequency;
        return this;
    }

    public DistributionJobModelBuilder processingType(String processingType) {
        this.processingType = EnumUtils.getEnum(ProcessingType.class, processingType);
        return this;
    }

    public DistributionJobModelBuilder processingType(ProcessingType processingType) {
        this.processingType = processingType;
        return this;
    }

    public DistributionJobModelBuilder channelDescriptorName(String channelDescriptorName) {
        this.channelDescriptorName = channelDescriptorName;
        return this;
    }

    public DistributionJobModelBuilder createdAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public DistributionJobModelBuilder lastUpdated(OffsetDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
        return this;
    }

    public DistributionJobModelBuilder blackDuckGlobalConfigId(Long blackDuckGlobalConfigId) {
        this.blackDuckGlobalConfigId = blackDuckGlobalConfigId;
        return this;
    }

    public DistributionJobModelBuilder filterByProject(boolean filterByProject) {
        this.filterByProject = filterByProject;
        return this;
    }

    public DistributionJobModelBuilder projectNamePattern(String projectNamePattern) {
        this.projectNamePattern = projectNamePattern;
        return this;
    }

    public DistributionJobModelBuilder projectVersionNamePattern(String projectVersionNamePattern) {
        this.projectVersionNamePattern = projectVersionNamePattern;
        return this;
    }

    public DistributionJobModelBuilder notificationTypes(List<String> notificationTypes) {
        this.notificationTypes = notificationTypes;
        return this;
    }

    public DistributionJobModelBuilder projectFilterDetails(List<BlackDuckProjectDetailsModel> projectFilterDetails) {
        this.projectFilterDetails = projectFilterDetails;
        return this;
    }

    public DistributionJobModelBuilder policyFilterPolicyNames(List<String> policyFilterPolicyNames) {
        this.policyFilterPolicyNames = policyFilterPolicyNames;
        return this;
    }

    public DistributionJobModelBuilder vulnerabilityFilterSeverityNames(List<String> vulnerabilityFilterSeverityNames) {
        this.vulnerabilityFilterSeverityNames = vulnerabilityFilterSeverityNames;
        return this;
    }

    public DistributionJobDetailsModel getDistributionJobDetails() {
        return distributionJobDetails;
    }

    public DistributionJobModelBuilder distributionJobDetails(DistributionJobDetailsModel distributionJobDetails) {
        this.distributionJobDetails = distributionJobDetails;
        return this;
    }

    private void throwExceptionIfBlank(String field, String fieldName) {
        if (StringUtils.isBlank(field)) {
            throw createMissingFieldException(fieldName);
        }
    }

    private void throwExceptionIfNull(Object field, String fieldName) {
        if (null == field) {
            throw createMissingFieldException(fieldName);
        }
    }

    private AlertRuntimeException createMissingFieldException(String fieldName) {
        return new AlertRuntimeException(String.format("Missing required field: '%s'", fieldName));
    }

}
