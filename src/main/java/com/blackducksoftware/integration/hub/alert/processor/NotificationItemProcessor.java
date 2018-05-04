/**
 * hub-alert
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
package com.blackducksoftware.integration.hub.alert.processor;

import java.util.ArrayList;
import java.util.Collection;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.event.DBStoreEvent;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.ProjectService;
import com.blackducksoftware.integration.hub.throwaway.MapProcessorCache;
import com.blackducksoftware.integration.hub.throwaway.NotificationEvent;
import com.blackducksoftware.integration.hub.throwaway.NotificationProcessor;
import com.blackducksoftware.integration.hub.throwaway.PolicyOverrideContentItem;
import com.blackducksoftware.integration.hub.throwaway.PolicyViolationClearedContentItem;
import com.blackducksoftware.integration.hub.throwaway.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.throwaway.VulnerabilityContentItem;
import com.blackducksoftware.integration.log.IntLogger;

public class NotificationItemProcessor extends NotificationProcessor<DBStoreEvent> {

    public NotificationItemProcessor(final GlobalProperties globalProperties, final IntLogger intLogger) {
        init(globalProperties, intLogger);
    }

    public void init(final GlobalProperties globalProperties, final IntLogger intLogger) {
        HubServicesFactory hubServicesFactory;
        try {
            hubServicesFactory = globalProperties.createHubServicesFactory(intLogger);

            final ProjectService projectService = hubServicesFactory.createProjectService();
            final MapProcessorCache policyCache = new UserNotificationCache(projectService);
            final VulnerabilityCache vulnerabilityCache = new VulnerabilityCache(projectService, hubServicesFactory);
            getCacheList().add(policyCache);
            getCacheList().add(vulnerabilityCache);
            getProcessorMap().put(PolicyViolationContentItem.class, new PolicyViolationProcessor(policyCache, intLogger));
            getProcessorMap().put(PolicyViolationClearedContentItem.class, new PolicyViolationClearedProcessor(policyCache, intLogger));
            getProcessorMap().put(PolicyOverrideContentItem.class, new PolicyOverrideProcessor(policyCache, intLogger));
            getProcessorMap().put(VulnerabilityContentItem.class, new VulnerabilityProcessor(vulnerabilityCache, intLogger));
        } catch (final IntegrationException ex) {
            intLogger.error("Error building the notification processor", ex);
        }
    }

    @Override
    public DBStoreEvent processEvents(final Collection<NotificationEvent> eventList) throws HubIntegrationException {
        final DBStoreEvent dbStoreEvent = new DBStoreEvent(new ArrayList<>(eventList));
        return dbStoreEvent;
    }
}
