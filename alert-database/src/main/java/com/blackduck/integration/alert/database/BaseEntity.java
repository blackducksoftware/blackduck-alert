package com.blackduck.integration.alert.database;

import java.io.Serializable;

import com.blackduck.integration.util.Stringable;

import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BaseEntity extends Stringable implements Serializable {
    protected BaseEntity() {
        // JPA requires default constructor definitions
    }
}
