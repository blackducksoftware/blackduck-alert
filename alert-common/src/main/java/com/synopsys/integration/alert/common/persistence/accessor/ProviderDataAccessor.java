/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;

public interface ProviderDataAccessor {
    Optional<ProviderProject> findFirstByHref(final String href);

    Optional<ProviderProject> findFirstByName(final String name);

    List<ProviderProject> findByProviderName(final String providerName);

    ProviderProject saveProject(final String providerName, final ProviderProject providerProject);

    List<ProviderProject> saveProjects(final String providerName, final Collection<ProviderProject> providerProjects);

    void deleteProjects(final String providerName, final Collection<ProviderProject> providerProjects);

    void deleteByHref(final String projectHref);

    Set<String> getEmailAddressesForProjectHref(final String projectHref);

    void remapUsersToProjectByEmail(final String projectHref, final Collection<String> emailAddresses) throws AlertDatabaseConstraintException;

    List<ProviderUserModel> getAllUsers(final String providerName);

    List<ProviderUserModel> saveUsers(final String providerName, final Collection<ProviderUserModel> users);

    void deleteUsers(final String providerName, final Collection<ProviderUserModel> users);

    void updateProjectAndUserData(String providerName, final Map<ProviderProject, Set<String>> projectToUserData);
}
