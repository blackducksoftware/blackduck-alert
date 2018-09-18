/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.workflow.processor;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.synopsys.integration.alert.common.model.NotificationModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.notification.NotificationDetailResult;
import com.synopsys.integration.blackduck.service.bucket.HubBucket;

public abstract class NotificationTypeProcessor {
    private final Set<NotificationType> applicableNotificationTypes;

    public NotificationTypeProcessor(final Set<NotificationType> applicableNotificationTypes) {
        this.applicableNotificationTypes = applicableNotificationTypes;
    }

    public abstract List<NotificationModel> process(final BlackDuckProperties blackDuckProperties, final NotificationDetailResult notificationDetailResult, final HubBucket bucket);

    public Set<NotificationType> getApplicableNotificationTypes() {
        return Collections.unmodifiableSet(applicableNotificationTypes);
    }
}
