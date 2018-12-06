package com.synopsys.integration.alert.database.entity.descriptor;

import javax.persistence.Entity;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;

@Entity
// TODO @Table(schema = "ALERT", name = "CONFIG_CONTEXTS")
public class ConfigContextEntity extends DatabaseEntity {
    // TODO @Column(name = "CONTEXT")
    private String context;

    public ConfigContextEntity() {
        // JPA requires default constructor definitions
    }

    public ConfigContextEntity(final String context) {
        this.context = context;
    }

    public String getContext() {
        return context;
    }
}
