/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;

public interface ProviderDataAccessor {
    List<ProviderProject> getProjectsByProviderConfigName(String providerConfigName);

    List<ProviderProject> getProjectsByProviderConfigId(Long providerConfigId);

    AlertPagedModel<ProviderProject> getProjectsByProviderConfigId(Long providerConfigId, int pageNumber, int pageSize, String searchTerm);

    @Deprecated
    void deleteProjects(Collection<ProviderProject> providerProjects);

    Set<String> getEmailAddressesForProjectHref(Long providerConfigId, String projectHref);

    List<ProviderUserModel> getUsersByProviderConfigId(Long providerConfigId);

    AlertPagedModel<ProviderUserModel> getUsersByProviderConfigId(Long providerConfigId, int pageNumber, int pageSize, String searchTerm);

    List<ProviderUserModel> getUsersByProviderConfigName(String providerConfigName);

    @Deprecated
    void updateProjectAndUserData(Long providerConfigId, Map<ProviderProject, Set<String>> projectToUserData, Set<String> additionalRelevantUsers);

}
