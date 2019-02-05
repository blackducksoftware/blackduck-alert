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
package com.synopsys.integration.alert.database.provider.blackduck.data;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.database.RepositoryAccessor;

@Component
@Transactional
public class BlackDuckProjectRepositoryAccessor extends RepositoryAccessor<BlackDuckProjectEntity> {
    private static final int MAX_DESCRIPTION_LENGTH = 250;
    private final BlackDuckProjectRepository blackDuckProjectRepository;

    @Autowired
    public BlackDuckProjectRepositoryAccessor(final BlackDuckProjectRepository blackDuckProjectRepository) {
        super(blackDuckProjectRepository);
        this.blackDuckProjectRepository = blackDuckProjectRepository;
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public BlackDuckProjectEntity findByName(final String name) {
        return blackDuckProjectRepository.findByName(name);
    }

    @Override
    public BlackDuckProjectEntity saveEntity(final BlackDuckProjectEntity blackDuckProjectEntity) {
        BlackDuckProjectEntity passedBlackDuckProjectEntity = blackDuckProjectEntity;
        final String description = blackDuckProjectEntity.getDescription();
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            final String trimmedDescription = trimDescription(description);
            passedBlackDuckProjectEntity = new BlackDuckProjectEntity(blackDuckProjectEntity.getName(), trimmedDescription, blackDuckProjectEntity.getHref(), blackDuckProjectEntity.getProjectOwnerEmail());
        }
        return super.saveEntity(passedBlackDuckProjectEntity);
    }

    public List<BlackDuckProjectEntity> deleteAndSaveAll(final Iterable<BlackDuckProjectEntity> blackDuckProjectEntities) {
        blackDuckProjectRepository.deleteAllInBatch();
        return blackDuckProjectRepository.saveAll(blackDuckProjectEntities);
    }

    private String trimDescription(final String projectDescription) {
        final String trimmedDescription = StringUtils.substring(projectDescription, 0, MAX_DESCRIPTION_LENGTH);
        return trimmedDescription + "...";
    }
}
