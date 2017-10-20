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
package com.blackducksoftware.integration.hub.alert.channel.hipchat.datasource.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "hipchat_config", schema = "configuration")
public class HipChatConfigEntity implements Serializable {
    private static final long serialVersionUID = 9172607945030111585L;

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Column(name = "api_key")
    private String apiKey;

    @Column(name = "room_id")
    private Integer roomId;

    @Column(name = "notify")
    private Boolean notify;

    @Column(name = "color")
    private String color;

    protected HipChatConfigEntity() {
    }

    public HipChatConfigEntity(final Long id, final String apiKey, final Integer room_id, final Boolean notify, final String color) {
        this.id = id;
        this.apiKey = apiKey;
        this.roomId = room_id;
        this.notify = notify;
        this.color = color;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public Long getId() {
        return id;
    }

    public String getApiKey() {
        return apiKey;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public Boolean getNotify() {
        return notify;
    }

    public String getColor() {
        return color;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((apiKey == null) ? 0 : apiKey.hashCode());
        result = prime * result + ((color == null) ? 0 : color.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((notify == null) ? 0 : notify.hashCode());
        result = prime * result + ((roomId == null) ? 0 : roomId.hashCode());
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
        final HipChatConfigEntity other = (HipChatConfigEntity) obj;
        if (apiKey == null) {
            if (other.apiKey != null) {
                return false;
            }
        } else if (!apiKey.equals(other.apiKey)) {
            return false;
        }
        if (color == null) {
            if (other.color != null) {
                return false;
            }
        } else if (!color.equals(other.color)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (notify == null) {
            if (other.notify != null) {
                return false;
            }
        } else if (!notify.equals(other.notify)) {
            return false;
        }
        if (roomId == null) {
            if (other.roomId != null) {
                return false;
            }
        } else if (!roomId.equals(other.roomId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "HipChatConfigEntity [id=" + id + ", apiKey=<NOT_SHOWN>, room_id=" + roomId + ", notify=" + notify + ", color=" + color + "]";
    }

}
