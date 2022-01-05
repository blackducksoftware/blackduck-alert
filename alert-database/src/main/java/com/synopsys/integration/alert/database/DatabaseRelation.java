/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class DatabaseRelation extends BaseEntity {
    public DatabaseRelation() {
        super();
    }
}
