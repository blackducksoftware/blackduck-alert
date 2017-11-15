/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.datasource.relation.manager;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.HubUsersRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserFrequenciesRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.HubUserEmailRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.HubUserFrequenciesRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.HubUserHipChatRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.HubUserSlackRepository;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.HubUsersConfigWrapper;
import com.blackducksoftware.integration.hub.alert.web.model.ProjectVersionConfigWrapper;

@Component
public class HubUserManager {
    private final HubUsersRepository hubUsersRepository;
    private final HubUserFrequenciesRepository hubUserFrequenciesRepository;
    private final HubUserEmailRepository hubUserEmailRepository;
    private final HubUserHipChatRepository hubUserHipChatRepository;
    private final HubUserSlackRepository hubUserSlackRepository;
    private final ObjectTransformer objectTransformer;

    public HubUserManager(final HubUsersRepository hubUsersRepository, final HubUserFrequenciesRepository hubUserFrequenciesRepository, final HubUserEmailRepository hubUserEmailRepository,
            final HubUserHipChatRepository hubUserHipChatRepository, final HubUserSlackRepository hubUserSlackRepository, final ObjectTransformer objectTransformer) {
        this.hubUsersRepository = hubUsersRepository;
        this.hubUserFrequenciesRepository = hubUserFrequenciesRepository;
        this.hubUserEmailRepository = hubUserEmailRepository;
        this.hubUserHipChatRepository = hubUserHipChatRepository;
        this.hubUserSlackRepository = hubUserSlackRepository;
        this.objectTransformer = objectTransformer;
    }

    public Long saveConfig(final HubUsersConfigWrapper wrapper) throws AlertException {
        // TODO save to appropriate tables
        return 1L; // TODO remove
    }

    public void deleteConfig(final String id) {
        deleteConfig(objectTransformer.stringToLong(id));
    }

    public void deleteConfig(final Long id) {
        // TODO delete from the appropriate tables
    }

    public boolean doesConfigExist(final String id) {
        return doesConfigExist(objectTransformer.stringToLong(id));
    }

    public boolean doesConfigExist(final Long id) {
        return hubUsersRepository.exists(id);
    }

    public String getHubUserFrequency(final Long id) {
        final HubUserFrequenciesRelation frequenciesRelation = hubUserFrequenciesRepository.findOne(id);
        final Long freqId = frequenciesRelation.getFrequencyId();
        // TODO frequency repo
        // final String frequency = ;
        return freqId != null ? freqId.toString() : null;
    }

    public String getEmailConfigId() {
        return null; // TODO
    }

    public String getHipChatConfigId() {
        return null; // TODO
    }

    public String getSlackConfigId() {
        return null; // TODO
    }

    public List<ProjectVersionConfigWrapper> getProjectVersions() {
        return Collections.emptyList(); // TODO
    }

    public ObjectTransformer getObjectTransformer() {
        return objectTransformer;
    }

}
