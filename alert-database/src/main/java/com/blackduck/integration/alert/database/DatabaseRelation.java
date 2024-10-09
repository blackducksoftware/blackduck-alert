/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database;

import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class DatabaseRelation extends BaseEntity {
    protected DatabaseRelation() {
        super();
    }
}
