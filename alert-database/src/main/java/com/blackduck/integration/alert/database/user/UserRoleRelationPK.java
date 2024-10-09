package com.blackduck.integration.alert.database.user;

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
