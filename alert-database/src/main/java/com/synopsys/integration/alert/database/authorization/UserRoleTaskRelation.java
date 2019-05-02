package com.synopsys.integration.alert.database.authorization;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.DatabaseRelation;

@Entity
@IdClass(UserRoleTaskPK.class)
@Table(schema = "alert", name = "role_tasks")
public class UserRoleTaskRelation extends DatabaseRelation {
    @Id
    @Column(name = "role_id")
    private Long roleId;
    @Id
    @Column(name = "task_id")
    private Long taskId;

    public UserRoleTaskRelation() {
        // JPA requires default constructor definitions
    }

    public UserRoleTaskRelation(final Long roleId, final Long taskId) {
        this.roleId = roleId;
        this.taskId = taskId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public Long getTaskId() {
        return taskId;
    }
}
