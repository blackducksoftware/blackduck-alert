package com.synopsys.integration.alert.database.entity.descriptor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;

@Entity
@Table(schema = "ALERT", name = "REGISTERED_DESCRIPTORS")
public class RegisteredDescriptorsEntity extends DatabaseEntity {
    @Column(name = "NAME")
    private String name;
    @Column(name = "TYPE")
    private String type;

    public RegisteredDescriptorsEntity() {
        // JPA requires default constructor definitions
    }

    public RegisteredDescriptorsEntity(final String name, final String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
