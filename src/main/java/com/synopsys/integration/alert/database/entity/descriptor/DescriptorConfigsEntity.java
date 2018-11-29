package com.synopsys.integration.alert.database.entity.descriptor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;

@Entity
@Table(schema = "ALERT", name = "DESCRIPTOR_CONFIGS")
public class DescriptorConfigsEntity extends DatabaseEntity {
    @Column(name = "DESCRIPTOR_ID")
    private Long descriptorId;

    public DescriptorConfigsEntity() {
        // JPA requires default constructor definitions
    }

    public DescriptorConfigsEntity(final Long descriptorId) {
        this.descriptorId = descriptorId;
    }

    public Long getDescriptorId() {
        return descriptorId;
    }
}
