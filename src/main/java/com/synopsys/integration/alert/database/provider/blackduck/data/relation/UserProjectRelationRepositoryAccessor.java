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
package com.synopsys.integration.alert.database.provider.blackduck.data.relation;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserProjectRelationRepositoryAccessor {
    private final UserProjectRelationRepository userProjectRelationRepository;

    @Autowired
    public UserProjectRelationRepositoryAccessor(final UserProjectRelationRepository userProjectRelationRepository) {
        this.userProjectRelationRepository = userProjectRelationRepository;
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<UserProjectRelation> readEntities() {
        return userProjectRelationRepository.findAll();
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<UserProjectRelation> findByBlackDuckProjectId(final Long blackDuckProjectId) {
        return userProjectRelationRepository.findByBlackDuckProjectId(blackDuckProjectId);
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<UserProjectRelation> findByBlackDuckUserId(final Long blackDuckUserId) {
        return userProjectRelationRepository.findByBlackDuckUserId(blackDuckUserId);
    }

    @Transactional
    public List<UserProjectRelation> deleteAndSaveAll(final Set<UserProjectRelation> userProjectRelations) {
        userProjectRelationRepository.deleteAllInBatch();
        return userProjectRelationRepository.saveAll(userProjectRelations);
    }
}
