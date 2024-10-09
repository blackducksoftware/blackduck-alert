package com.blackduck.integration.alert.api.provider.lifecycle;

import java.util.List;

import com.blackduck.integration.alert.api.provider.state.ProviderProperties;

public interface ProviderTaskFactory {
    List<ProviderTask> createTasks(ProviderProperties providerProperties);

}
