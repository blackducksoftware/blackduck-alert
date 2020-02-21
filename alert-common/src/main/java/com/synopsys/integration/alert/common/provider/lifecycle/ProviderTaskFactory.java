package com.synopsys.integration.alert.common.provider.lifecycle;

import java.util.List;

import com.synopsys.integration.alert.common.provider.ProviderProperties;

public interface ProviderTaskFactory {
    List<ProviderTask> createTasks(ProviderProperties providerProperties);
}
