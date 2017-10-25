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
package com.blackducksoftware.integration.hub.alert.channel.slack;

import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;

public class SlackEvent extends AbstractChannelEvent {

    public SlackEvent(final ProjectData projectData) {
        super(projectData);
        // TODO Auto-generated constructor stub
    }

    @Override
    public String getTopic() {
        // TODO Auto-generated method stub
        return null;
    }

}
