/*
 * web
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.api.about;

import java.util.Set;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.descriptor.config.ui.DescriptorMetadata;

public class AboutModel extends AlertSerializableModel {
    private String version;
    private String created;
    private String description;
    private String projectUrl;
    private String documentationUrl;
    private boolean initialized;
    private String startupTime;
    private Set<DescriptorMetadata> providers;
    private Set<DescriptorMetadata> channels;

    protected AboutModel() {

    }

    public AboutModel(
        String version,
        String created,
        String description,
        String projectUrl,
        String documentationUrl,
        boolean initialized,
        String startupTime,
        Set<DescriptorMetadata> providers,
        Set<DescriptorMetadata> channels
    ) {
        this.version = version;
        this.created = created;
        this.description = description;
        this.projectUrl = projectUrl;
        this.documentationUrl = documentationUrl;
        this.initialized = initialized;
        this.startupTime = startupTime;
        this.providers = providers;
        this.channels = channels;
    }

    public String getVersion() {
        return version;
    }

    public String getCreated() {
        return created;
    }

    public String getDescription() {
        return description;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public String getDocumentationUrl() {
        return documentationUrl;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public String getStartupTime() {
        return startupTime;
    }

    public Set<DescriptorMetadata> getProviders() {
        return providers;
    }

    public Set<DescriptorMetadata> getChannels() {
        return channels;
    }

}
