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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.BaseEntity;
import com.synopsys.integration.alert.database.DatabaseEntity;

@Entity
@Table(schema = "alert", name = "field_values")
public class FieldValueEntity extends BaseEntity implements DatabaseEntity {
    @Id
    @GeneratedValue(generator = "alert.field_values_id_seq_generator", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "alert.field_values_id_seq_generator", sequenceName = "alert.field_values_id_seq")
    @Column(name = "id")
    private Long id;
    @Column(name = "config_id")
    private Long configId;
    @Column(name = "field_id")
    private Long fieldId;
    @Column(name = "field_value")
    private String value;

    @ManyToOne
    @JoinColumn(name = "field_id", referencedColumnName = "id", insertable = false, updatable = false)
    private DefinedFieldEntity definedFieldEntity;

    @ManyToOne
    @JoinColumn(name = "config_id", referencedColumnName = "id", insertable = false, updatable = false)
    private DescriptorConfigEntity descriptorConfigEntity;

    public FieldValueEntity() {
        // JPA requires default constructor definitions
    }

    public FieldValueEntity(Long configId, Long fieldId, String value) {
        this.configId = configId;
        this.fieldId = fieldId;
        this.value = value;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
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
