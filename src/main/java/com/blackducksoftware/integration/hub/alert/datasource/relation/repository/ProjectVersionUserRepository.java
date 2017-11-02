/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.alert.datasource.relation.repository;

import java.util.Collection;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.blackducksoftware.integration.hub.alert.datasource.relation.ProjectVersionUserRelation;

@Transactional
public interface ProjectVersionUserRepository extends JpaRepository<ProjectVersionUserRelation, Long> {
    @Query("SELECT ProjectVersionUserRelation projectVersionUser FROM project_version_user WHERE projectVersionUser.id = ?1")
    public Collection<ProjectVersionUserRelation> findByUserId(final Long id);
}
