/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model.job;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class FilteredDistributionJobRequestModelV2 extends AlertSerializableModel {
    private Collection<FrequencyType> frequencyTypes;
    private Set<String> projectNames;
    private Set<String> notificationTypes;
    private Set<String> vulnerabilitySeverities;
    private Set<String> policyNames;

    public FilteredDistributionJobRequestModelV2(
        Collection<FrequencyType> frequencyTypes,
        Set<String> projectNames,
        Set<String> notificationTypes,
        Set<String> vulnerabilitySeverities,
        Set<String> policyNames
    ) {
        this.frequencyTypes = frequencyTypes;
        this.projectNames = projectNames;
        this.notificationTypes = notificationTypes;
        this.vulnerabilitySeverities = vulnerabilitySeverities;
        this.policyNames = policyNames;
    }

    public Collection<FrequencyType> getFrequencyTypes() {
        return frequencyTypes;
    }

    public Set<String> getProjectName() {
        return projectNames;
    }

    public Set<String> getNotificationTypes() {
        return notificationTypes;
    }

    public Optional<Set<String>> getVulnerabilitySeverities() {
        //may not need this, check if empty in the accessor.
        if (vulnerabilitySeverities.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(vulnerabilitySeverities);
    }

    public Optional<Set<String>> getPolicyNames() {
        if (policyNames.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(policyNames);
    }

    public boolean isVulnerabilityNotification() {
        return !getVulnerabilitySeverities().isEmpty();
    }

    public boolean isPolicyNotification() {
        return !getPolicyNames().isEmpty();
    }
}

