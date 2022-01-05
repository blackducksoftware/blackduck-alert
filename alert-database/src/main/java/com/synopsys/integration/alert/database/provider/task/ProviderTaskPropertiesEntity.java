/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.provider.task;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.BaseEntity;

@Entity
@IdClass(ProviderTaskPropertiesEntityPK.class)
@Table(schema = "alert", name = "provider_task_properties")
public class ProviderTaskPropertiesEntity extends BaseEntity {
    @Column(name = "provider_config_id")
    private Long providerConfigId;

    @Id
    @Column(name = "task_name")
    private String taskName;

    @Id
    @Column(name = "property_name")
    private String propertyName;

    @Column(name = "value")
    private String value;

    public ProviderTaskPropertiesEntity() {
        // JPA requires default constructor definitions
    }

    public ProviderTaskPropertiesEntity(Long providerConfigId, String taskName, String propertyName, String value) {
        this.providerConfigId = providerConfigId;
        this.taskName = taskName;
        this.propertyName = propertyName;
        this.value = value;
    }

    public Long getProviderConfigId() {
        return providerConfigId;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getValue() {
        return value;
    }

}
