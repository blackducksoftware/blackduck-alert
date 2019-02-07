/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
import javax.persistence.Table;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;

@Entity
@Table(schema = "alert", name = "users")
public class UserEntity extends DatabaseEntity {
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

    public UserEntity() {
        // JPA requires default constructor definitions
    }

    public UserEntity(final String userName, final String password, final String emailAddress) {
        this.userName = userName;
        this.password = password;
        this.emailAddress = emailAddress;
        this.expired = false;
        this.locked = false;
        this.passwordExpired = false;
        this.enabled = true;
    }

    public UserEntity(final String userName, final String password, final String emailAddress, final boolean expired, final boolean locked, final boolean passwordExpired, final boolean enabled) {
        this.userName = userName;
        this.password = password;
        this.emailAddress = emailAddress;
        this.expired = expired;
        this.locked = locked;
        this.passwordExpired = passwordExpired;
        this.enabled = enabled;
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
}
