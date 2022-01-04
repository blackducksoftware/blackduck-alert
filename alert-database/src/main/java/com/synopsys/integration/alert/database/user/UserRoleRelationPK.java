/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.user;

import java.io.Serializable;

public class UserRoleRelationPK implements Serializable {
    private static final long serialVersionUID = 1857555560045339402L;
    private Long userId;
    private Long roleId;

    public UserRoleRelationPK() {
        // JPA requires default constructor definitions
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}
