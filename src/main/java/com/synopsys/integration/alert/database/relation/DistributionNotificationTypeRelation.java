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
package com.synopsys.integration.alert.database.relation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.relation.key.DistributionNotificationTypeRelationPK;

@Entity
@IdClass(DistributionNotificationTypeRelationPK.class)
@Table(schema = "alert", name = "distribution_notification_types")
public class DistributionNotificationTypeRelation extends DatabaseRelation {
    @Id
    @Column
    private Long commonDistributionConfigId;

    @Id
    @Column
    private String notificationType;

    public DistributionNotificationTypeRelation() {
        // JPA requires default constructor definitions
    }

    public DistributionNotificationTypeRelation(final Long commonDistributionConfigId, final String notificationType) {
        this.commonDistributionConfigId = commonDistributionConfigId;
        this.notificationType = notificationType;
    }

    public Long getCommonDistributionConfigId() {
        return commonDistributionConfigId;
    }

    public String getNotificationType() {
        return notificationType;
    }

}
