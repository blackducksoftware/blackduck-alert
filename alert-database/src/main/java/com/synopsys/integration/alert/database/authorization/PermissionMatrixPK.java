/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.authorization;

import java.io.Serializable;

public class PermissionMatrixPK implements Serializable {
    private static final long serialVersionUID = -7208709612139559784L;
    private Long roleId;
    private Long contextId;
    private Long descriptorId;
    private Integer operations;

    public PermissionMatrixPK() {
        // JPA requires default constructor definitions
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getContextId() {
        return contextId;
    }

    public void setContextId(Long contextId) {
        this.contextId = contextId;
    }

    public Long getDescriptorId() {
        return descriptorId;
    }

    public void setDescriptorId(Long descriptorId) {
        this.descriptorId = descriptorId;
    }

    public Integer getOperations() {
        return operations;
    }

    public void setOperations(Integer operations) {
        this.operations = operations;
    }

}
