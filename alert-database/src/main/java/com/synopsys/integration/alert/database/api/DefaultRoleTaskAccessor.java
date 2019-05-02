/**
 * alert-database
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
package com.synopsys.integration.alert.database.api;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.rest.model.RolePermissionModel;
import com.synopsys.integration.alert.database.authorization.PermissionKeyEntity;
import com.synopsys.integration.alert.database.authorization.PermissionKeyRepository;
import com.synopsys.integration.alert.database.authorization.PermissionMatrixRelation;
import com.synopsys.integration.alert.database.authorization.PermissionMatrixRepository;
import com.synopsys.integration.alert.database.user.RoleEntity;
import com.synopsys.integration.alert.database.user.RoleRepository;

@Component
@Transactional
public class DefaultRoleTaskAccessor {
    private final RoleRepository roleRepository;
    private final PermissionKeyRepository permissionKeyRepository;
    private final PermissionMatrixRepository roleTaskRepository;

    @Autowired
    public DefaultRoleTaskAccessor(final RoleRepository roleRepository, final PermissionKeyRepository permissionKeyRepository, final PermissionMatrixRepository roleTaskRepository) {
        this.roleRepository = roleRepository;
        this.permissionKeyRepository = permissionKeyRepository;
        this.roleTaskRepository = roleTaskRepository;
    }

    public void assignTaskToRole(final String role, final RolePermissionModel roleTask) {
        final List<RoleEntity> roles = roleRepository.findRoleEntitiesByRoleName(List.of(role));
        final Optional<PermissionKeyEntity> userTask = permissionKeyRepository.findByKeyName(roleTask.getPermissionKey());

        if (userTask.isPresent()) {
            final PermissionKeyEntity taskEntity = userTask.get();
            final Collection<PermissionMatrixRelation> roleTaskRelations = new LinkedList<>();
            for (final RoleEntity databaseRole : roles) {
                roleTaskRelations.add(new PermissionMatrixRelation(databaseRole.getId(), taskEntity.getId()));
            }
            roleTaskRepository.saveAll(roleTaskRelations);
        }
    }
}
