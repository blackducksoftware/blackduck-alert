/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "settings", schema = "configuration")
public class Setting {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Column(name = "setting_name")
    private String settingName;

    @Column(name = "setting_value")
    private String settingValue;

    @Column(name = "setting_type")
    private String settingType;

    public Setting(final String settingName, final String settingValue, final String settingType) {
        super();
        this.settingName = settingName;
        this.settingValue = settingValue;
        this.settingType = settingType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((settingName == null) ? 0 : settingName.hashCode());
        result = prime * result + ((settingType == null) ? 0 : settingType.hashCode());
        result = prime * result + ((settingValue == null) ? 0 : settingValue.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Setting other = (Setting) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (settingName == null) {
            if (other.settingName != null) {
                return false;
            }
        } else if (!settingName.equals(other.settingName)) {
            return false;
        }
        if (settingType == null) {
            if (other.settingType != null) {
                return false;
            }
        } else if (!settingType.equals(other.settingType)) {
            return false;
        }
        if (settingValue == null) {
            if (other.settingValue != null) {
                return false;
            }
        } else if (!settingValue.equals(other.settingValue)) {
            return false;
        }
        return true;
    }

    public String getSettingName() {
        return settingName;
    }

    public void setSettingName(final String settingName) {
        this.settingName = settingName;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(final String settingValue) {
        this.settingValue = settingValue;
    }

    public String getSettingType() {
        return settingType;
    }

    public void setSettingType(final String settingType) {
        this.settingType = settingType;
    }

    public Long getId() {
        return id;
    }

}
