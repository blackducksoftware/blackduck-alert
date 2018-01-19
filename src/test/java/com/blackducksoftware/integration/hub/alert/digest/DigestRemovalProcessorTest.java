/*
 * Copyright (C) 2018 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.alert.digest;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.VulnerabilityEntity;
import com.blackducksoftware.integration.hub.alert.enumeration.VulnerabilityOperationEnum;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;

public class DigestRemovalProcessorTest {

    @Test
    public void processVulnerabilitiesTest() {
        final DigestRemovalProcessor digestRemovalProcessor = new DigestRemovalProcessor();
        final List<NotificationEntity> processed = digestRemovalProcessor.process(createVulnerabilityNotifications());
        assertEquals(3, processed.size());
    }

    @Test
    public void processPoliciesTest() {
        final DigestRemovalProcessor digestRemovalProcessor = new DigestRemovalProcessor();
        final List<NotificationEntity> processed = digestRemovalProcessor.process(createPolicyNotifications());
        assertEquals(1, processed.size());
    }

    @Test
    public void processSingleTest() {
        final DigestRemovalProcessor digestRemovalProcessor = new DigestRemovalProcessor();
        final List<NotificationEntity> processed = digestRemovalProcessor.process(Arrays.asList(createVulnerabilityNotifications().get(0)));
        assertEquals(1, processed.size());
    }

    private List<NotificationEntity> createPolicyNotifications() {
        final String key = "key";
        final String projectName = "Project Name";
        final String projectVersion = "Project Version";
        final NotificationEntity notification1 = new NotificationEntity(key, null, NotificationCategoryEnum.POLICY_VIOLATION, projectName, "", projectVersion, "", "", "", "", "", Collections.emptyList());
        final NotificationEntity notification2 = new NotificationEntity(key, null, NotificationCategoryEnum.POLICY_VIOLATION_CLEARED, projectName, "", projectVersion, "", "", "", "", "", Collections.emptyList());
        final NotificationEntity notification3 = new NotificationEntity(key, null, NotificationCategoryEnum.POLICY_VIOLATION_OVERRIDE, projectName, "", projectVersion, "", "", "", "", "", Collections.emptyList());
        final NotificationEntity notification4 = new NotificationEntity(key, null, NotificationCategoryEnum.POLICY_VIOLATION, projectName, "", projectVersion, "", "", "", "", "", Collections.emptyList());
        final NotificationEntity notification5 = new NotificationEntity(key, null, NotificationCategoryEnum.POLICY_VIOLATION_CLEARED, projectName, "", projectVersion, "", "", "", "", "", Collections.emptyList());

        return Arrays.asList(notification1, notification2, notification1, notification3, notification2, notification4, notification5);
    }

    private List<NotificationEntity> createVulnerabilityNotifications() {
        final String keyPrefix = "key";
        final String projectName = "Project Name";
        final String projectVersion = "Project Version";
        final NotificationEntity notification1 = new NotificationEntity(keyPrefix + "1", null, NotificationCategoryEnum.HIGH_VULNERABILITY, projectName, "", projectVersion, "", "", "", "", "", createVulnerabilityList());
        final NotificationEntity notification2 = new NotificationEntity(keyPrefix + "2", null, NotificationCategoryEnum.MEDIUM_VULNERABILITY, projectName, "", projectVersion, "", "", "", "", "", createVulnerabilityList());
        final NotificationEntity notification3 = new NotificationEntity(keyPrefix + "3", null, NotificationCategoryEnum.LOW_VULNERABILITY, projectName, "", projectVersion, "", "", "", "", "", createVulnerabilityList());
        final NotificationEntity notification4 = new NotificationEntity(keyPrefix + "4", null, NotificationCategoryEnum.VULNERABILITY, projectName, "", projectVersion, "", "", "", "", "", Collections.emptyList());

        return Arrays.asList(notification1, notification2, notification1, notification3, notification2, notification4);
    }

    private List<VulnerabilityEntity> createVulnerabilityList() {
        final VulnerabilityEntity vuln1 = new VulnerabilityEntity("id1", VulnerabilityOperationEnum.ADD);
        final VulnerabilityEntity vuln2 = new VulnerabilityEntity("id2", VulnerabilityOperationEnum.DELETE);
        final VulnerabilityEntity vuln3 = new VulnerabilityEntity("id3", VulnerabilityOperationEnum.UPDATE);

        return Arrays.asList(vuln1, vuln2, vuln1, vuln3, vuln2);
    }

}
