package com.blackduck.integration.alert.api.provider;

import java.util.List;

import com.blackduck.integration.alert.common.rest.model.JobProviderProjectFieldModel;

public interface ProviderProjectExistencePopulator {
    void populateJobProviderProjects(Long providerGlobalConfigId, List<JobProviderProjectFieldModel> configuredProviderProjects);

}
