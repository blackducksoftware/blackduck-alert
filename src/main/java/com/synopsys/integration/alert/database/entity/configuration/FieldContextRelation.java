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
package com.synopsys.integration.alert.database.entity.configuration;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.relation.key.FieldContextRelationPK;

@Entity
@IdClass(FieldContextRelationPK.class)
@Table(schema = "ALERT", name = "FIELD_CONTEXTS")
public class FieldContextRelation {
    @Id
    @Column(name = "FIELD_ID")
    private Long fieldId;
    @Id
    @Column(name = "CONTEXT_ID")
    private Long contextId;

    public FieldContextRelation() {
        // JPA requires default constructor definitions
    }

    public FieldContextRelation(final Long fieldId, final Long contextId) {
        this.fieldId = fieldId;
        this.contextId = contextId;
    }

    public Long getFieldId() {
        return fieldId;
    }

    public Long getContextId() {
        return contextId;
    }

}
