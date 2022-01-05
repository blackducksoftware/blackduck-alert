/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.extract.model;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;

public class SimpleMessage extends ProviderMessage<SimpleMessage> {
    private final String summary;
    private final String description;
    private final List<LinkableItem> details;

    private final ProjectMessage source;

    public static SimpleMessage original(ProviderDetails providerDetails, String summary, String description, List<LinkableItem> details) {
        return new SimpleMessage(providerDetails, summary, description, details, null);
    }

    public static SimpleMessage derived(String summary, String description, List<LinkableItem> details, ProjectMessage source) {
        return new SimpleMessage(source.getProviderDetails(), summary, description, details, source);
    }

    private SimpleMessage(ProviderDetails provider, String summary, String description, List<LinkableItem> details, @Nullable ProjectMessage source) {
        super(provider);
        this.summary = summary;
        this.description = description;
        this.details = details;
        this.source = source;
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }

    public List<LinkableItem> getDetails() {
        return details;
    }

    public Optional<ProjectMessage> getSource() {
        return Optional.ofNullable(source);
    }

    @Override
    public List<SimpleMessage> combine(SimpleMessage otherMessage) {
        return List.of(this, otherMessage);
    }

}
