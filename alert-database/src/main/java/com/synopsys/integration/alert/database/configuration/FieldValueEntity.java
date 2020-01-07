/**
 * alert-database
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
package com.synopsys.integration.alert.database.configuration;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.DatabaseEntity;

@Entity
@Table(schema = "ALERT", name = "FIELD_VALUES")
public class FieldValueEntity extends DatabaseEntity {
    @Column(name = "CONFIG_ID")
    private Long configId;
    @Column(name = "FIELD_ID")
    private Long fieldId;
    @Column(name = "FIELD_VALUE")
    private String value;

    @ManyToOne
    @JoinColumn(name = "FIELD_ID", referencedColumnName = "ID", insertable = false, updatable = false)
    private DefinedFieldEntity definedFieldEntity;

    @ManyToOne
    @JoinColumn(name = "CONFIG_ID", referencedColumnName = "ID", insertable = false, updatable = false)
    private DescriptorConfigEntity descriptorConfigEntity;

    public FieldValueEntity() {
        // JPA requires default constructor definitions
    }

    public FieldValueEntity(final Long configId, final Long fieldId, final String value) {
        this.configId = configId;
        this.fieldId = fieldId;
        this.value = value;
    }

    public Long getConfigId() {
        return configId;
    }

    public Long getFieldId() {
        return fieldId;
    }

    public String getValue() {
        return value;
    }

    public DefinedFieldEntity getDefinedFieldEntity() {
        return definedFieldEntity;
    }

    public DescriptorConfigEntity getDescriptorConfigEntity() {
        return descriptorConfigEntity;
    }
}
