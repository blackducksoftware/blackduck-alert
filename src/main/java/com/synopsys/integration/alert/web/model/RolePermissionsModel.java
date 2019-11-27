/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.web.model;

import java.util.Map;
import java.util.Set;

import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class RolePermissionsModel extends AlertSerializableModel {
    private String roleName;
    private Map<PermissionKey, Set<String>> operations;

    public RolePermissionsModel() {
    }

    public RolePermissionsModel(String roleName, Map<PermissionKey, Set<String>> operations) {
        this.roleName = roleName;
        this.operations = operations;
    }

    public String getRoleName() {
        return roleName;
    }

    public Map<PermissionKey, Set<String>> getOperations() {
        return operations;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void setOperations(Map<PermissionKey, Set<String>> operations) {
        this.operations = operations;
    }
}
