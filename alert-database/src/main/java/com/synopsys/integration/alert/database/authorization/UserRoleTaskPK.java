package com.synopsys.integration.alert.database.authorization;

import java.io.Serializable;

public class UserRoleTaskPK implements Serializable {
    private static final long serialVersionUID = -5317479456840456710L;
    private Long roleId;
    private Long taskId;

    public UserRoleTaskPK() {
        // JPA requires default constructor definitions
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(final Long roleId) {
        this.roleId = roleId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(final Long taskId) {
        this.taskId = taskId;
    }
}
