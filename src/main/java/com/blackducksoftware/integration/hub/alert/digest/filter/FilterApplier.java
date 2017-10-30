/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.alert.digest.filter;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;

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
        final Set<ProjectData> usersProcessed = userFilter.getRelevantItems(projectDataItems);
        final Set<ProjectData> projectsProcessed = projectFilter.getRelevantItems(usersProcessed);
        final Set<ProjectData> projectVersionsProcessed = projectVersionFilter.getRelevantItems(projectsProcessed);

        return getChannelEvents(projectVersionsProcessed);
    }

    public List<AbstractChannelEvent> getChannelEvents(final Collection<ProjectData> projectDataItems) {
        projectDataItems.forEach(item -> {
            // TODO filter channels
        });

        return channelEvents;
    }

}
