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
package com.synopsys.integration.alert.database.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.BaseEntity;
import com.synopsys.integration.alert.database.DatabaseEntity;

@Entity
@Table(schema = "alert", name = "roles")
public class RoleEntity extends BaseEntity implements DatabaseEntity {
    private static final long serialVersionUID = 7928926209935268556L;
    @Id
    @GeneratedValue(generator = "alert.roles_id_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "alert.roles_id_seq", sequenceName = "alert.roles_id_seq")
    @Column(name = "id")
    private Long id;
    @Column(name = "rolename")
    private String roleName;
    @Column(name = "custom")
    private Boolean custom;

    public RoleEntity() {
        // JPA requires default constructor definitions
    }

    public RoleEntity(String roleName, Boolean custom) {
        this.roleName = roleName;
        this.custom = custom;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public Boolean getCustom() {
        return custom;
    }

}
