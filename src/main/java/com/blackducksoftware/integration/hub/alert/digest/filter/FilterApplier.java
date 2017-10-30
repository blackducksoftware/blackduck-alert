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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;

/*
 * Approach #1
 *
 * I don't think this way will be efficient at all, or work the way we want it to.
 */
public class FilterApplier {
    private final List<AbstractChannelEvent> channelEvents;
    private final UserFilter userFilter;
    private final ProjectFilter projectFilter;
    private final ProjectVersionFilter projectVersionFilter;

    public FilterApplier(final UserFilter userFilter, final ProjectFilter projectFilter, final ProjectVersionFilter projectVersionFilter) {
        this.channelEvents = new LinkedList<>();
        this.userFilter = userFilter;
        this.projectFilter = projectFilter;
        this.projectVersionFilter = projectVersionFilter;
    }

    public List<AbstractChannelEvent> applyFilters(final Collection<ProjectData> projectDataItems) {
        final Set<ProjectData> usersProcessed = userFilter.getFilteredItems(projectDataItems);
        final Set<ProjectData> projectsProcessed = projectFilter.getFilteredItems(usersProcessed);
        final Set<ProjectData> projectVersionsProcessed = projectVersionFilter.getFilteredItems(projectsProcessed);

        return getChannelEvents(projectVersionsProcessed);
    }

    public List<AbstractChannelEvent> getChannelEvents(final Collection<ProjectData> projectDataItems) {
        projectDataItems.forEach(item -> {
            // TODO filter channels
        });

        return channelEvents;
    }

}
