/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRoleRepository extends JpaRepository<UserRoleRelation, UserRoleRelationPK> {

    // find all role ids for a user
    List<UserRoleRelation> findAllByUserId(Long userId);

    // find all user ids for a role
    List<UserRoleRelation> findAllByRoleId(Long roleId);

    // delete the relations by user id
    @Query("DELETE FROM UserRoleRelation userRoleRelation"
               + " WHERE userRoleRelation.userId = :userId"
    )
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void bulkDeleteAllByUserId(@Param("userId") Long userId);
}
