/*
 * api-provider
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.provider;

import java.util.List;

import com.blackduck.integration.alert.common.rest.model.JobProviderProjectFieldModel;

public interface ProviderProjectExistencePopulator {
    void populateJobProviderProjects(Long providerGlobalConfigId, List<JobProviderProjectFieldModel> configuredProviderProjects);

}
