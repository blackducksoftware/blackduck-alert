/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.processor.digest;

import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.processor.extract.model.CombinableModel;
import com.synopsys.integration.alert.api.processor.extract.model.ProcessedProviderMessage;
import com.synopsys.integration.alert.api.processor.extract.model.project.ProjectMessage;

@Component
public class ProjectMessageDigester {
    public List<ProcessedProviderMessage<ProjectMessage>> digest(List<ProcessedProviderMessage<ProjectMessage>> processedProjectMessages) {
        return CombinableModel.combine(processedProjectMessages);
    }

}
