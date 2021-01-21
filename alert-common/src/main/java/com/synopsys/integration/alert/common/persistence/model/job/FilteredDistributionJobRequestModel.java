/**
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.common.persistence.model.job;

import java.util.List;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

public class FilteredDistributionJobRequestModel extends AlertSerializableModel {
    private List<FrequencyType> frequencyTypes;
    private NotificationType notificationType;
    private String projectName;
    private List<String> vulnerabilitySeverities;
    private List<String> policyNames;

    public FilteredDistributionJobRequestModel(
        List<FrequencyType> frequencyTypes,
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

    public List<FrequencyType> getFrequencyTypes() {
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
}
