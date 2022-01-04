/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.authorization;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionMatrixRepository extends JpaRepository<PermissionMatrixRelation, PermissionMatrixPK> {

    List<PermissionMatrixRelation> findAllByRoleId(Long roleId);

    List<PermissionMatrixRelation> findAllByRoleIdIn(Collection<Long> roleIds);

}
