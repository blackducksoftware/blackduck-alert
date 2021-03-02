/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRoleRelation, UserRoleRelationPK> {

    // find all role ids for a user
    List<UserRoleRelation> findAllByUserId(Long userId);

    // find all user ids for a role
    List<UserRoleRelation> findAllByRoleId(Long roleId);

    // delete the relations by user id
    void deleteAllByUserId(Long userId);
}
