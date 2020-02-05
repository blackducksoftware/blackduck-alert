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
package com.synopsys.integration.alert.database.system;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.BaseEntity;

@Entity
@Table(schema = "alert", name = "system_status")
public class SystemStatus extends BaseEntity {
    private static final long serialVersionUID = -5482465786237355472L;

    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "initialized_configuration")
    private boolean initialConfigurationPerformed;
    @Column(name = "startup_time")
    private Date startupTime;

    public SystemStatus() {
        //JPA requires a default constructor
    }

    public SystemStatus(boolean initialConfigurationPerformed, Date startupTime) {
        this.initialConfigurationPerformed = initialConfigurationPerformed;
        this.startupTime = startupTime;
    }

    public boolean isInitialConfigurationPerformed() {
        return initialConfigurationPerformed;
    }

    public Date getStartupTime() {
        return startupTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
