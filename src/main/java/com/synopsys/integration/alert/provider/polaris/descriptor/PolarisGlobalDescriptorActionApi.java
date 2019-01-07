package com.synopsys.integration.alert.provider.polaris.descriptor;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.descriptor.config.context.DescriptorActionApi;
import com.synopsys.integration.alert.web.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class PolarisGlobalDescriptorActionApi extends DescriptorActionApi {
    @Override
    public void validateConfig(final FieldAccessor fieldAccessor, final Map<String, String> fieldErrors) {
        // FIXME implement
    }

    @Override
    public void testConfig(final TestConfigModel testConfig) throws IntegrationException {
        // FIXME implement
    }
}
