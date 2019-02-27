/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.web.provider.blackduck;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.database.api.ProviderDataAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;

@Component
public class BlackDuckDataActions {
    private final Logger logger = LoggerFactory.getLogger(BlackDuckDataActions.class);
    private final ProviderDataAccessor blackDuckProjectRepositoryAccessor;

    @Autowired
    public BlackDuckDataActions(final ProviderDataAccessor blackDuckProjectRepositoryAccessor) {
        this.blackDuckProjectRepositoryAccessor = blackDuckProjectRepositoryAccessor;
    }

    public List<ProviderProject> getBlackDuckProjects() {
        final List<ProviderProject> providerProject = blackDuckProjectRepositoryAccessor.findByProviderName(BlackDuckProvider.COMPONENT_NAME);
        if (providerProject.isEmpty()) {
            logger.info("No BlackDuck projects found in the database.");
        }
        return providerProject;
    }

}
