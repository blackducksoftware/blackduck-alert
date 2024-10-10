/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.authorization;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionMatrixRepository extends JpaRepository<PermissionMatrixRelation, PermissionMatrixPK> {

    List<PermissionMatrixRelation> findAllByRoleId(Long roleId);

    List<PermissionMatrixRelation> findAllByRoleIdIn(Collection<Long> roleIds);

}
