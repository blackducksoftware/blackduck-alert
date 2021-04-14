package com.synopsys.integration.alert.common.persistence.model.job;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class FilteredDistributionJobRequestModel extends AlertSerializableModel {
    private Collection<FrequencyType> frequencyTypes;
    private Set<String> projectNames;
    private Set<String> notificationTypes;
    private Set<String> vulnerabilitySeverities;
    private Set<String> policyNames;

    public FilteredDistributionJobRequestModel(
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
        //TODO: Determine if we want to return without the optional
        //public Set<String> getVulnerabilitySeverities() {
        //return this.vulnerabilitySeverities.isEmpty() ? null : this.vulnerabilitySeverities;
        if (vulnerabilitySeverities.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(vulnerabilitySeverities);
    }

    public Optional<Set<String>> getPolicyNames() {
        //TODO: Determine if we want to return without the optional
        //public Set<String> getPolicyNames() {
        //return this.policyNames.isEmpty() ? null : this.policyNames;
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

