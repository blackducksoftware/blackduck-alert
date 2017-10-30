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
import java.util.HashSet;
import java.util.Set;

import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;

public class ProjectFilter implements NotificationFilter<ProjectData> {

    @Override
    public Set<ProjectData> getRelevantItems(final Collection<ProjectData> items) {
        // TODO
        return new HashSet<>(items);
    }
}
