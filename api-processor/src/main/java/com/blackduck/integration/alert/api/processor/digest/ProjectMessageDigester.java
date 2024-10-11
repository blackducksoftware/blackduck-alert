/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor.digest;

import java.util.List;

import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.processor.extract.model.CombinableModel;
import com.blackduck.integration.alert.api.processor.extract.model.ProcessedProviderMessage;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectMessage;

@Component
public class ProjectMessageDigester {
    public List<ProcessedProviderMessage<ProjectMessage>> digest(List<ProcessedProviderMessage<ProjectMessage>> processedProjectMessages) {
        return CombinableModel.combine(processedProjectMessages);
    }

}
