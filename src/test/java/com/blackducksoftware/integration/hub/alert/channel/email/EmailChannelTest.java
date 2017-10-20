package com.blackducksoftware.integration.hub.alert.channel.email;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.VulnerabilityEntity;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;
import com.google.gson.Gson;

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

public class EmailChannelTest {

    @Test
    public void sendEmailTest() {
        final List<VulnerabilityEntity> vulns = new ArrayList<>();
        final VulnerabilityEntity vulnerability = new VulnerabilityEntity("Vuln ID", "Vuln Operation");
        vulns.add(vulnerability);
        final NotificationEntity notification = new NotificationEntity("EventKey", new Date(), NotificationCategoryEnum.POLICY_VIOLATION.toString(), "Manual Test Project", "Manual Test Project Version", "Manual Test Component", "1.0.3",
                "Manual Policy Rule", vulns);

        final Gson gson = new Gson();
        final EmailChannel emailChannel = new EmailChannel(gson);
        final EmailEvent event = new EmailEvent(notification);
        emailChannel.recieveMessage(gson.toJson(event));
    }
}
