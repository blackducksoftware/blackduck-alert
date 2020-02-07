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

import java.io.Serializable;

public class PermissionMatrixPK implements Serializable {
    private static final long serialVersionUID = -5317479456840456710L;
    private Long roleId;
    private Long permissionKeyId;
    private Long accessOperationId;

    public PermissionMatrixPK() {
        // JPA requires default constructor definitions
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(final Long roleId) {
        this.roleId = roleId;
    }

    public Long getPermissionKeyId() {
        return permissionKeyId;
    }

    public void setPermissionKeyId(final Long permissionKeyId) {
        this.permissionKeyId = permissionKeyId;
    }

    public Long getAccessOperationId() {
        return accessOperationId;
    }

    public void setAccessOperationId(final Long accessOperationId) {
        this.accessOperationId = accessOperationId;
    }
}
