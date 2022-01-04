/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;

import com.synopsys.integration.util.Stringable;

@MappedSuperclass
public abstract class BaseEntity extends Stringable implements Serializable {
    public BaseEntity() {
        // JPA requires default constructor definitions
    }
}
