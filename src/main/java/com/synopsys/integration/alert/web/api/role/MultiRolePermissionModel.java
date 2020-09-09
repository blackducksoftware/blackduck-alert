/**
 * blackduck-alert
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
package com.synopsys.integration.alert.web.api.role;

import java.util.List;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class MultiRolePermissionModel extends AlertSerializableModel {
    private final List<RolePermissionModel> roles;

    MultiRolePermissionModel() {
        // For serialization
        this.roles = List.of();
    }

    public MultiRolePermissionModel(List<RolePermissionModel> roles) {
        this.roles = roles;
    }

    public List<RolePermissionModel> getRoles() {
        return roles;
    }

}
