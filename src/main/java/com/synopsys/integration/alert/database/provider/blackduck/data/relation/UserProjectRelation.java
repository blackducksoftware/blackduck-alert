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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.relation.DatabaseRelation;

@Entity
@IdClass(UserProjectRelationPK.class)
@Table(schema = "alert", name = "blackduck_user_project_relation")
public class UserProjectRelation extends DatabaseRelation {
    @Id
    @Column(name = "blackduck_user_id")
    private Long blackDuckUserId;

    @Id
    @Column(name = "blackduck_project_id")
    private Long blackDuckProjectId;

    public UserProjectRelation() {
        // JPA requires default constructor definitions
    }

    public UserProjectRelation(final Long blackDuckUserId, final Long blackDuckProjectId) {
        super();
        this.blackDuckUserId = blackDuckUserId;
        this.blackDuckProjectId = blackDuckProjectId;
    }

    public Long getBlackDuckUserId() {
        return blackDuckUserId;
    }

    public Long getBlackDuckProjectId() {
        return blackDuckProjectId;
    }
}
