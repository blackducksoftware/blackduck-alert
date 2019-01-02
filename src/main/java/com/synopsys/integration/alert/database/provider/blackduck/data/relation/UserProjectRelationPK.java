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

import java.io.Serializable;

public class UserProjectRelationPK implements Serializable {
    private static final long serialVersionUID = 2978750766498759769L;
    private Long blackDuckUserId;
    private Long blackDuckProjectId;

    public UserProjectRelationPK() {
        // JPA requires default constructor definitions
    }

    public Long getBlackDuckUserId() {
        return blackDuckUserId;
    }

    public void setBlackDuckUserId(final Long blackDuckUserId) {
        this.blackDuckUserId = blackDuckUserId;
    }

    public Long getBlackDuckProjectId() {
        return blackDuckProjectId;
    }

    public void setBlackDuckProjectId(final Long blackDuckProjectId) {
        this.blackDuckProjectId = blackDuckProjectId;
    }
}
