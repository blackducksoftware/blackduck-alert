/*
 * blackduck-alert
 *
 * Copyright (c) 2025 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database;

import java.io.Serializable;

import org.springframework.data.domain.Persistable;

import com.blackduck.integration.util.Stringable;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Transient;

@MappedSuperclass
public abstract class BasePersistableEntity<T> extends Stringable implements Persistable<T>, Serializable {

    @Id
    @Column(name = "notification_id")
    private T notificationId;

    @Transient
    private boolean isNew;

    protected BasePersistableEntity() {
        this.isNew = true;
    }

    protected BasePersistableEntity(T notificationId) {
        this.notificationId = notificationId;
        this.isNew = true;
    }

    @Override
    public T getId() {
        return notificationId;
    }

    public void setId(T id) {
        this.notificationId = id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    protected void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    @PostPersist
    @PostLoad
    protected void markNotNew() {
        this.isNew = false;
    }
}
