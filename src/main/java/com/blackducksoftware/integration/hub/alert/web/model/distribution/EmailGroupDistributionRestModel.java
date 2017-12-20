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
package com.blackducksoftware.integration.hub.alert.web.model.distribution;

import java.util.Date;
import java.util.List;

import com.blackducksoftware.integration.hub.alert.enumeration.StatusEnum;

public class EmailGroupDistributionRestModel extends CommonDistributionConfigRestModel {
    private static final long serialVersionUID = -4728604702503057780L;

    private String groupName;

    public EmailGroupDistributionRestModel() {
    }

    public EmailGroupDistributionRestModel(final String id, final String distributionConfigId, final String distributionType, final String name, final String frequency, final String filterByProject, final String groupName,
            final List<String> configuredProjects, final List<String> notificationTypes, final Date lastRan, final StatusEnum status) {
        super(id, distributionConfigId, distributionType, name, frequency, filterByProject, configuredProjects, notificationTypes, lastRan, status);
        this.groupName = groupName;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(final String groupName) {
        this.groupName = groupName;
    }

}
