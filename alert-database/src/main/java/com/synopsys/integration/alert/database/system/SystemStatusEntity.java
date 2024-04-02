/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.system;

import java.time.OffsetDateTime;

import com.synopsys.integration.alert.database.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "alert", name = "system_status")
public class SystemStatusEntity extends BaseEntity {
    private static final long serialVersionUID = -5482465786237355472L;

    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "initialized_configuration")
    private boolean initialConfigurationPerformed;
    @Column(name = "startup_time")
    private OffsetDateTime startupTime;

    public SystemStatusEntity() {
        //JPA requires a default constructor
    }

    public SystemStatusEntity(boolean initialConfigurationPerformed, OffsetDateTime startupTime) {
        this.initialConfigurationPerformed = initialConfigurationPerformed;
        this.startupTime = startupTime;
    }

    public boolean isInitialConfigurationPerformed() {
        return initialConfigurationPerformed;
    }

    public OffsetDateTime getStartupTime() {
        return startupTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
