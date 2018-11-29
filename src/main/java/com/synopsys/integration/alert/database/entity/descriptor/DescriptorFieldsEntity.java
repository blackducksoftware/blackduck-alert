package com.synopsys.integration.alert.database.entity.descriptor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;

@Entity
@Table(schema = "ALERT", name = "DESCRIPTOR_FIELDS")
public class DescriptorFieldsEntity extends DatabaseEntity {
    @Column(name = "DESCRIPTOR_ID")
    private Long descriptorId;
    @Column(name = "SOURCE_KEY")
    private String key;
    @Column(name = "SENSITIVE")
    private Boolean sensitive;

    public DescriptorFieldsEntity() {
        // JPA requires default constructor definitions
    }

    public DescriptorFieldsEntity(final Long descriptorId, final String key, final Boolean sensitive) {
        this.descriptorId = descriptorId;
        this.key = key;
        this.sensitive = sensitive;
    }

    public Long getDescriptorId() {
        return descriptorId;
    }

    public String getKey() {
        return key;
    }

    public Boolean getSensitive() {
        return sensitive;
    }
}
