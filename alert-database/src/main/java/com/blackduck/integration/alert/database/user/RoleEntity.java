package com.blackduck.integration.alert.database.user;

import com.blackduck.integration.alert.database.BaseEntity;
import com.blackduck.integration.alert.database.DatabaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "alert", name = "roles")
public class RoleEntity extends BaseEntity implements DatabaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "rolename")
    private String roleName;
    @Column(name = "custom")
    private Boolean custom;

    public RoleEntity() {
        // JPA requires default constructor definitions
    }

    public RoleEntity(String roleName, Boolean custom) {
        this.roleName = roleName;
        this.custom = custom;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public Boolean getCustom() {
        return custom;
    }

}
