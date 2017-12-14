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
package com.blackducksoftware.integration.hub.alert.datasource.relation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.blackducksoftware.integration.hub.alert.datasource.relation.key.DistributionNotificationTypeRelationPK;

@Entity
@IdClass(DistributionNotificationTypeRelationPK.class)
@Table(schema = "alert", name = "distribution_notification_type_relation")
public class DistributionNotificationTypeRelation extends DatabaseRelation {
    private static final long serialVersionUID = 8419397449988298455L;

    @Column
    private Long commonDistributionConfigId;

    @Column
    private Long notificationTypeId;

    public DistributionNotificationTypeRelation() {

    }

    public DistributionNotificationTypeRelation(final Long commonDistributionConfigId, final Long notificationTypeId) {
        this.commonDistributionConfigId = commonDistributionConfigId;
        this.notificationTypeId = notificationTypeId;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public Long getCommonDistributionConfigId() {
        return commonDistributionConfigId;
    }

    public Long getNotificationTypeId() {
        return notificationTypeId;
    }

}
