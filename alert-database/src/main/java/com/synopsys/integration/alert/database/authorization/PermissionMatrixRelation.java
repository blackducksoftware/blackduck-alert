/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.authorization;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.DatabaseRelation;

@Entity
@IdClass(PermissionMatrixPK.class)
@Table(schema = "alert", name = "permission_matrix")
public class PermissionMatrixRelation extends DatabaseRelation {
    @Id
    @Column(name = "role_id")
    private Long roleId;
    @Id
    @Column(name = "context_id")
    private Long contextId;
    @Id
    @Column(name = "descriptor_id")
    private Long descriptorId;
    @Id
    @Column(name = "operations")
    private Integer operations;

    public PermissionMatrixRelation() {
        // JPA requires default constructor definitions
    }

    public PermissionMatrixRelation(Long roleId, Long contextId, Long descriptorId, Integer operations) {
        this.roleId = roleId;
        this.contextId = contextId;
        this.descriptorId = descriptorId;
        this.operations = operations;
    }

    public Long getRoleId() {
        return roleId;
    }

    public Long getContextId() {
        return contextId;
    }

    public Long getDescriptorId() {
        return descriptorId;
    }

    public Integer getOperations() {
        return operations;
    }

}
