/*
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.extract.model;

import java.util.List;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;

public class ProcessedProviderMessageHolder extends AlertSerializableModel {
    private final List<ProcessedProviderMessage<ProjectMessage>> processedProjectMessages;
    private final List<ProcessedProviderMessage<SimpleMessage>> processedSimpleMessages;

    public static ProcessedProviderMessageHolder empty() {
        return new ProcessedProviderMessageHolder(List.of(), List.of());
    }

    public ProcessedProviderMessageHolder(
        List<ProcessedProviderMessage<ProjectMessage>> processedProjectMessages,
        List<ProcessedProviderMessage<SimpleMessage>> processedSimpleMessages
    ) {
        this.processedProjectMessages = processedProjectMessages;
        this.processedSimpleMessages = processedSimpleMessages;
    }

    public List<ProcessedProviderMessage<ProjectMessage>> getProcessedProjectMessages() {
        return processedProjectMessages;
    }

    public List<ProcessedProviderMessage<SimpleMessage>> getProcessedSimpleMessages() {
        return processedSimpleMessages;
    }

}
