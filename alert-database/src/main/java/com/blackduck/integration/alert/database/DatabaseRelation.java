package com.blackduck.integration.alert.database;

import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class DatabaseRelation extends BaseEntity {
    protected DatabaseRelation() {
        super();
    }
}
