/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.extract.model;

import java.util.List;

import org.apache.commons.collections4.ListUtils;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;

public class ProviderMessageHolder extends AlertSerializableModel {
    private final List<ProjectMessage> projectMessages;
    private final List<SimpleMessage> simpleMessages;

    public static ProviderMessageHolder empty() {
        return new ProviderMessageHolder(List.of(), List.of());
    }

    public static ProviderMessageHolder reduce(ProviderMessageHolder lhs, ProviderMessageHolder rhs) {
        List<ProjectMessage> unifiedProjectMessages = ListUtils.union(lhs.getProjectMessages(), rhs.getProjectMessages());
        List<SimpleMessage> unifiedSimpleMessages = ListUtils.union(lhs.getSimpleMessages(), rhs.getSimpleMessages());
        return new ProviderMessageHolder(unifiedProjectMessages, unifiedSimpleMessages);
    }

    public ProviderMessageHolder(List<ProjectMessage> projectMessages, List<SimpleMessage> simpleMessages) {
        this.projectMessages = projectMessages;
        this.simpleMessages = simpleMessages;
    }

    public List<ProjectMessage> getProjectMessages() {
        return projectMessages;
    }

    public List<SimpleMessage> getSimpleMessages() {
        return simpleMessages;
    }

}
