/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;

public interface ProviderDataAccessor {
    Optional<ProviderProject> getProjectByHref(Long providerConfigId, String projectHref);

    List<ProviderProject> getProjectsByProviderConfigName(String providerConfigName);

    List<ProviderProject> getProjectsByProviderConfigId(Long providerConfigId);

    AlertPagedModel<ProviderProject> getProjectsByProviderConfigId(Long providerConfigId, int pageNumber, int pageSize, String searchTerm);

    @Deprecated
    void deleteProjects(Collection<ProviderProject> providerProjects);

    Set<String> getEmailAddressesForProjectHref(Long providerConfigId, String projectHref);

    ProviderUserModel getProviderConfigUserById(Long providerConfigId) throws AlertConfigurationException;

    List<ProviderUserModel> getUsersByProviderConfigId(Long providerConfigId);

    AlertPagedModel<ProviderUserModel> getUsersByProviderConfigId(Long providerConfigId, int pageNumber, int pageSize, String searchTerm);

    List<ProviderUserModel> getUsersByProviderConfigName(String providerConfigName);

    @Deprecated
    void updateProjectAndUserData(Long providerConfigId, Map<ProviderProject, Set<String>> projectToUserData, Set<String> additionalRelevantUsers);

}
