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
package com.synopsys.integration.alert.database.authorization;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.DatabaseRelation;

@Entity
@IdClass(PermissionMatrixPK.class)
@Table(schema = "alert", name = "permission_matrix")
public class PermissionMatrixRelation extends DatabaseRelation {
    @Id
    @Column(name = "role_id")
    private Long roleId;
    @Id
    @Column(name = "permission_key_id")
    private Long permissionKeyId;
    @Id
    @Column(name = "access_operation_id")
    private Long accessOperationId;

    public PermissionMatrixRelation() {
        // JPA requires default constructor definitions
    }

    public PermissionMatrixRelation(final Long roleId, final Long permissionKeyId, final Long accessOperationId) {
        this.roleId = roleId;
        this.permissionKeyId = permissionKeyId;
        this.accessOperationId = accessOperationId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public Long getPermissionKeyId() {
        return permissionKeyId;
    }

    public Long getAccessOperationId() {
        return accessOperationId;
    }
}
