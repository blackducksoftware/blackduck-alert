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
package com.blackducksoftware.integration.hub.alert.digest.filter;

import java.util.List;
import java.util.Set;

import com.blackducksoftware.integration.hub.alert.datasource.entity.UserConfigEntity;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;

public class UserNotifications {
    private final UserConfigEntity userConfigEntity;
    private final Set<ProjectData> notifications;

    public UserNotifications(final UserConfigEntity userConfigEntity, final Set<ProjectData> notifications) {
        this.userConfigEntity = userConfigEntity;
        this.notifications = notifications;
    }

    public Long getUserConfigId() {
        return userConfigEntity.getId();
    }

    public List<String> getHubUsernames() {
        return StringUtils.formatCsv(userConfigEntity.getHubUsernames());
    }

    public Set<ProjectData> getNotifications() {
        return notifications;
    }

}
