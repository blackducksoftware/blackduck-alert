/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model.job;

import java.util.Collection;
import java.util.List;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

public class FilteredDistributionJobRequestModel extends AlertSerializableModel {
    private Collection<FrequencyType> frequencyTypes;
    private NotificationType notificationType;
    private String projectName;
    private List<String> vulnerabilitySeverities;
    private List<String> policyNames;

    public FilteredDistributionJobRequestModel(
        Collection<FrequencyType> frequencyTypes,
        NotificationType notificationType,
        String projectName,
        List<String> vulnerabilitySeverities,
        List<String> policyNames
    ) {
        this.frequencyTypes = frequencyTypes;
        this.notificationType = notificationType;
        this.projectName = projectName;
        this.vulnerabilitySeverities = vulnerabilitySeverities;
        this.policyNames = policyNames;
    }

    public Collection<FrequencyType> getFrequencyTypes() {
        return frequencyTypes;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public String getProjectName() {
        return projectName;
    }

    public List<String> getVulnerabilitySeverities() {
        return vulnerabilitySeverities;
    }

    public List<String> getPolicyNames() {
        return policyNames;
    }

    public boolean isVulnerabilityNotification() {
        return !getVulnerabilitySeverities().isEmpty();
    }

    public boolean isPolicyNotification() {
        return !getPolicyNames().isEmpty();
    }
}
