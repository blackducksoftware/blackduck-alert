/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.model.job;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.alert.common.enumeration.FrequencyType;

public class FilteredDistributionJobRequestModel extends AlertSerializableModel {
    private static final long serialVersionUID = -8714694795622818776L;
    private final Long providerConfigId;
    private final Long notificationId;
    private final List<FrequencyType> frequencyTypes;
    private final Set<String> projectNames = new HashSet<>();
    private final Set<String> projectVersionNames = new HashSet<>();
    private final Set<String> notificationTypes = new HashSet<>();
    private final Set<String> vulnerabilitySeverities = new HashSet<>();
    private final Set<String> policyNames = new HashSet<>();

    public FilteredDistributionJobRequestModel(Long providerConfigId, List<FrequencyType> frequencyTypes) {
        this(providerConfigId, null, frequencyTypes);
    }

    public FilteredDistributionJobRequestModel(Long providerConfigId, Long notificationId, List<FrequencyType> frequencyTypes) {
        this.providerConfigId = providerConfigId;
        this.notificationId = notificationId;
        this.frequencyTypes = frequencyTypes;
    }

    public void addProjectName(String projectName) {
        projectNames.add(projectName);
    }

    public void addProjectVersionName(String projectName) {
        projectVersionNames.add(projectName);
    }

    public void addNotificationType(String notificationType) {
        notificationTypes.add(notificationType);
    }

    public void addVulnerabilitySeverities(Collection<String> vulnerabilitySeverities) {
        this.vulnerabilitySeverities.addAll(vulnerabilitySeverities);
    }

    public void addPolicyName(String policyName) {
        policyNames.add(policyName);
    }

    public Long getProviderConfigId() {
        return providerConfigId;
    }

    //TODO remove optional when notification processing is replaced.
    public Optional<Long> getNotificationId() {
        return Optional.ofNullable(notificationId);
    }

    public Collection<FrequencyType> getFrequencyTypes() {
        return frequencyTypes;
    }

    public Set<String> getProjectName() {
        return projectNames;
    }

    public Set<String> getProjectVersionNames() {
        return projectVersionNames;
    }

    public Set<String> getNotificationTypes() {
        return notificationTypes;
    }

    public Set<String> getVulnerabilitySeverities() {
        return vulnerabilitySeverities;
    }

    public Set<String> getPolicyNames() {
        return policyNames;
    }

    public boolean isVulnerabilityNotification() {
        return !getVulnerabilitySeverities().isEmpty();
    }

    public boolean isPolicyNotification() {
        return !getPolicyNames().isEmpty();
    }

}

