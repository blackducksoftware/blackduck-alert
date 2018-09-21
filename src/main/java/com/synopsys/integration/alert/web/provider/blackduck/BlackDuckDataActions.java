/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.model.BlackDuckProject;

@Component
public class BlackDuckDataActions {
    private final Logger logger = LoggerFactory.getLogger(BlackDuckDataActions.class);
    private final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor;

    @Autowired
    public BlackDuckDataActions(final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor) {
        this.blackDuckProjectRepositoryAccessor = blackDuckProjectRepositoryAccessor;
    }

    public List<BlackDuckProject> getBlackDuckProjects() {
        final List<BlackDuckProjectEntity> blackDuckProjectEntities = (List<BlackDuckProjectEntity>) blackDuckProjectRepositoryAccessor.readEntities();
        if (!blackDuckProjectEntities.isEmpty()) {
            final List<BlackDuckProject> projects = new ArrayList<>();
            for (final BlackDuckProjectEntity blackDuckProjectEntity : blackDuckProjectEntities) {
                final BlackDuckProject project = new BlackDuckProject(blackDuckProjectEntity.getName(), blackDuckProjectEntity.getDescription(), blackDuckProjectEntity.getHref());
                projects.add(project);
            }
            return projects;
        } else {
            logger.info("No BlackDuck projects found in the database.");
        }
        return Collections.emptyList();
    }

}
