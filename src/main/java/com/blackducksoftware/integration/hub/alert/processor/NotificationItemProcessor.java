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
package com.blackducksoftware.integration.hub.alert.processor;

import java.util.ArrayList;
import java.util.Collection;

import com.blackducksoftware.integration.hub.alert.event.DBStoreEvent;
import com.blackducksoftware.integration.hub.api.project.ProjectAssignmentService;
import com.blackducksoftware.integration.hub.api.project.ProjectService;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.api.vulnerability.VulnerabilityService;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyOverrideContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyViolationClearedContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.VulnerabilityContentItem;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.notification.processor.MapProcessorCache;
import com.blackducksoftware.integration.hub.notification.processor.NotificationProcessor;
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.log.IntLogger;

public class NotificationItemProcessor extends NotificationProcessor<DBStoreEvent> {

    public NotificationItemProcessor(final ProjectService projectService, final ProjectAssignmentService projectAssignMentRequestService, final HubService hubService, final VulnerabilityService vulnerabilityService,
            final IntLogger intLogger) {
        init(projectService, projectAssignMentRequestService, hubService, vulnerabilityService, intLogger);
    }

    public void init(final ProjectService projectService, final ProjectAssignmentService projectAssignMentRequestService, final HubService hubResponseService, final VulnerabilityService vulnerabilityRequestService,
            final IntLogger intLogger) {
        final MapProcessorCache policyCache = new UserNotificationCache(projectService, projectAssignMentRequestService);
        final VulnerabilityCache vulnerabilityCache = new VulnerabilityCache(projectService, projectAssignMentRequestService, hubResponseService, vulnerabilityRequestService, new MetaHandler(intLogger));
        getCacheList().add(policyCache);
        getCacheList().add(vulnerabilityCache);
        getProcessorMap().put(PolicyViolationContentItem.class, new PolicyViolationProcessor(policyCache, intLogger));
        getProcessorMap().put(PolicyViolationClearedContentItem.class, new PolicyViolationClearedProcessor(policyCache, intLogger));
        getProcessorMap().put(PolicyOverrideContentItem.class, new PolicyOverrideProcessor(policyCache, intLogger));
        getProcessorMap().put(VulnerabilityContentItem.class, new VulnerabilityProcessor(vulnerabilityCache, intLogger));
    }

    @Override
    public DBStoreEvent processEvents(final Collection<NotificationEvent> eventList) throws HubIntegrationException {
        final DBStoreEvent dbStoreEvent = new DBStoreEvent(new ArrayList<>(eventList));
        return dbStoreEvent;
    }
}
