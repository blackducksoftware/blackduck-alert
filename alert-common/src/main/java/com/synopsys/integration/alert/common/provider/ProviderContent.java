package com.synopsys.integration.alert.common.provider;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.synopsys.integration.alert.common.enumeration.FormatType;

public abstract class ProviderContent {
    private final String providerName;
    private final Set<ProviderContentType> providerContentTypes;
    private final Set<FormatType> supportedContentFormats;

    public ProviderContent(final String providerName, final Set<ProviderContentType> providerContentTypes, final Set<FormatType> supportedContentFormats) {
        this.providerName = providerName;
        this.providerContentTypes = providerContentTypes;
        this.supportedContentFormats = supportedContentFormats;
    }

    public String getProviderName() {
        return providerName;
    }

    public Set<ProviderContentType> getContentTypes() {
        return ImmutableSet.copyOf(providerContentTypes);
    }

    public Set<FormatType> getSupportedContentFormats() {
        return ImmutableSet.copyOf(supportedContentFormats);
    }

}
