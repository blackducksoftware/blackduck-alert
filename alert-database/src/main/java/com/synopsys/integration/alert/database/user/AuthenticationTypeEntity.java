package com.synopsys.integration.alert.database.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.DatabaseEntity;

@Entity
@Table(schema = "alert", name = "authentication_type")
public class AuthenticationTypeEntity extends DatabaseEntity {
    @Column(name = "name")
    public String name;

    public AuthenticationTypeEntity() {
        // JPA requires default constructor definitions
    }

    public String getName() {
        return name;
    }
}
