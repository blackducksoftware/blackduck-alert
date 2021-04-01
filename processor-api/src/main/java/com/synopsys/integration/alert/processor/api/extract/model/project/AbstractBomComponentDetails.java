/*
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.extract.model.project;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public abstract class AbstractBomComponentDetails extends AlertSerializableModel {
    private final LinkableItem component;
    private final LinkableItem componentVersion;

    private final LinkableItem license;
    private final String usage;
    private final List<LinkableItem> additionalAttributes;

    private final String blackDuckIssuesUrl;

    protected AbstractBomComponentDetails(
        LinkableItem component,
        @Nullable LinkableItem componentVersion,
        LinkableItem license,
        String usage,
        List<LinkableItem> additionalAttributes,
        String blackDuckIssuesUrl
    ) {
        this.component = component;
        this.componentVersion = componentVersion;
        this.license = license;
        this.usage = usage;
        this.additionalAttributes = additionalAttributes;
        this.blackDuckIssuesUrl = blackDuckIssuesUrl;
    }

    public LinkableItem getComponent() {
        return component;
    }

    public Optional<LinkableItem> getComponentVersion() {
        return Optional.ofNullable(componentVersion);
    }

    public LinkableItem getLicense() {
        return license;
    }

    public String getUsage() {
        return usage;
    }

    public List<LinkableItem> getAdditionalAttributes() {
        return additionalAttributes;
    }

    // TODO make optional
    public String getBlackDuckIssuesUrl() {
        return blackDuckIssuesUrl;
    }

}
