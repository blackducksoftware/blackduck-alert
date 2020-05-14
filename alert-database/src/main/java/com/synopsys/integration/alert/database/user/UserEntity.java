/**
 * alert-database
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.database.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.BaseEntity;
import com.synopsys.integration.alert.database.DatabaseEntity;

@Entity
@Table(schema = "alert", name = "users")
public class UserEntity extends BaseEntity implements DatabaseEntity {
    @Id
    @GeneratedValue(generator = "alert.users_id_seq_generator", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "alert.users_id_seq_generator", sequenceName = "alert.users_id_seq")
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
