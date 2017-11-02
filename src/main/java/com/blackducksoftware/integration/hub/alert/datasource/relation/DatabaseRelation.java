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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class DatabaseRelation implements Serializable {
    private static final long serialVersionUID = 4000317230253976836L;

    @Column(name = "user_config_id")
    private final Long userConfidId;

    public DatabaseRelation(final Long userConfidId) {
        this.userConfidId = userConfidId;
    }

    public Long getUserConfidId() {
        return userConfidId;
    }

}
