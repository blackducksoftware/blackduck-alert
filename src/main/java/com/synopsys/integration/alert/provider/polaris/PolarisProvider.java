package com.synopsys.integration.alert.provider.polaris;

import java.util.EnumSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.provider.Provider;
import com.synopsys.integration.alert.common.provider.ProviderContentType;

@Component(PolarisProvider.COMPONENT_NAME)
public class PolarisProvider extends Provider {
    public static final String COMPONENT_NAME = "provider_polaris";

    public PolarisProvider() {
        super(PolarisProvider.COMPONENT_NAME);
    }

    @Override
    public void initialize() {
        // FIXME schedule Polaris tasks
    }

    @Override
    public void destroy() {
        // FIXME unschedule Polaris tasks
    }

    @Override
    public Set<ProviderContentType> getProviderContentTypes() {
        // FIXME create content types for this provider
        return Set.of();
    }

    @Override
    public Set<FormatType> getSupportedFormatTypes() {
        return EnumSet.of(FormatType.DEFAULT, FormatType.DIGEST);
    }
}
