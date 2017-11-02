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
package com.blackducksoftware.integration.hub.alert.datasource.relation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "project_version_user", schema = "user")
public class ProjectVersionUserRelation extends DatabaseRelation {
    private static final long serialVersionUID = 544672444719776792L;

    @Column(name = "project_name")
    private final String projectName;

    @Column(name = "project_version_name")
    private final String projectVersionName;

    public ProjectVersionUserRelation(final Long userConfidId, final String projectName, final String projectVersionName) {
        super(userConfidId);
        this.projectName = projectName;
        this.projectVersionName = projectVersionName;
    }

}
