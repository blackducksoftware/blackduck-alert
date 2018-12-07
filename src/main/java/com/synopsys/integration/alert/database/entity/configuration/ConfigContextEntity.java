package com.synopsys.integration.alert.database.entity.configuration;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;

@Entity
@Table(schema = "ALERT", name = "CONFIG_CONTEXTS")
public class ConfigContextEntity extends DatabaseEntity {
    @Column(name = "CONTEXT")
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
