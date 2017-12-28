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
package com.blackducksoftware.integration.hub.alert;

import java.util.Collections;
import java.util.Date;

import com.blackducksoftware.integration.hub.alert.channel.SupportedChannels;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;

public class MockUtils {

    public CommonDistributionConfigEntity createCommonDistributionConfigEntity() {
        return new CommonDistributionConfigEntity(null, SupportedChannels.HIPCHAT, "Job Name", "REAL_TIME", Boolean.FALSE);
    }

    public NotificationEntity createNotificationEntity() {
        return new NotificationEntity("_event_key_", new Date(), "POLICY_VIOLATION", "Test Project Name", "", "Test Project Version Name", "", "Component Name", "Component Version Name", "Policy Rule Name", "Person",
                Collections.emptyList());
    }

}
