package com.synopsys.integration.alert.common.provider;

import java.util.List;

import com.synopsys.integration.alert.common.rest.model.JobProviderProjectFieldModel;

public interface ProviderProjectExistencePopulator {
    void populateJobProviderProjects(Long providerGlobalConfigId, List<JobProviderProjectFieldModel> configuredProviderProjects);

}
