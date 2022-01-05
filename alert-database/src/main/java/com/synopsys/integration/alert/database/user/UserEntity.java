/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.BaseEntity;
import com.synopsys.integration.alert.database.DatabaseEntity;

@Entity
@Table(schema = "alert", name = "users")
public class UserEntity extends BaseEntity implements DatabaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "username")
    private String userName;
    @Column(name = "password")
    private String password;
    @Column(name = "email_address")
    private String emailAddress;
    @Column(name = "expired")
    private boolean expired;
    @Column(name = "locked")
    private boolean locked;
    @Column(name = "password_expired")
    private boolean passwordExpired;
    @Column(name = "enabled")
    private boolean enabled;
    @Column(name = "auth_type")
    private Long authenticationType;

    public UserEntity() {
        // JPA requires default constructor definitions
    }

    public UserEntity(String userName, String password, String emailAddress, Long authenticationType) {
        this.userName = userName;
        this.password = password;
        this.emailAddress = emailAddress;
        this.expired = false;
        this.locked = false;
        this.passwordExpired = false;
        this.enabled = true;
        this.authenticationType = authenticationType;
    }

    public UserEntity(String userName, String password, String emailAddress, boolean expired, boolean locked, boolean passwordExpired, boolean enabled, Long authenticationType) {
        this.userName = userName;
        this.password = password;
        this.emailAddress = emailAddress;
        this.expired = expired;
        this.locked = locked;
        this.passwordExpired = passwordExpired;
        this.enabled = enabled;
        this.authenticationType = authenticationType;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getPassword() {
        return this.password;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public boolean isExpired() {
        return this.expired;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public boolean isPasswordExpired() {
        return this.passwordExpired;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public Long getAuthenticationType() {
        return authenticationType;
    }
}
