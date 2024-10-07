/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database;

import java.io.Serializable;

import com.blackduck.integration.util.Stringable;

import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BaseEntity extends Stringable implements Serializable {
    protected BaseEntity() {
        // JPA requires default constructor definitions
    }
}
