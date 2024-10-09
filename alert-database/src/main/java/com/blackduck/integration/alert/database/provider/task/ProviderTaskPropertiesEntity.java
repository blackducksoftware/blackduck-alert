package com.blackduck.integration.alert.database.provider.task;

import com.blackduck.integration.alert.database.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

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
