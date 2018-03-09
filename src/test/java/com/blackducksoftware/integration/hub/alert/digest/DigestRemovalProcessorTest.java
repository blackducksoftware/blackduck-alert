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
import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;
import com.blackducksoftware.integration.hub.notification.NotificationCategoryEnum;

public class DigestRemovalProcessorTest {

    @Test
    public void processVulnerabilitiesTest() {
        final DigestRemovalProcessor digestRemovalProcessor = new DigestRemovalProcessor();
        final List<NotificationModel> processed = digestRemovalProcessor.process(createVulnerabilityNotifications());
        assertEquals(3, processed.size());
    }

    @Test
    public void processPoliciesTest() {
        final DigestRemovalProcessor digestRemovalProcessor = new DigestRemovalProcessor();
        final List<NotificationModel> processed = digestRemovalProcessor.process(createPolicyNotifications());
        assertEquals(1, processed.size());
    }

    @Test
    public void processSingleTest() {
        final DigestRemovalProcessor digestRemovalProcessor = new DigestRemovalProcessor();
        final List<NotificationModel> processed = digestRemovalProcessor.process(Arrays.asList(createVulnerabilityNotifications().get(0)));
        assertEquals(1, processed.size());
    }

    private List<NotificationModel> createPolicyNotifications() {
        final String key = "key";
        final String projectName = "Project Name";
        final String projectVersion = "Project Version";
        final NotificationEntity notification1 = new NotificationEntity(key, null, NotificationCategoryEnum.POLICY_VIOLATION, projectName, "", projectVersion, "", "", "", "", "");
        final NotificationEntity notification2 = new NotificationEntity(key, null, NotificationCategoryEnum.POLICY_VIOLATION_CLEARED, projectName, "", projectVersion, "", "", "", "", "");
        final NotificationEntity notification3 = new NotificationEntity(key, null, NotificationCategoryEnum.POLICY_VIOLATION_OVERRIDE, projectName, "", projectVersion, "", "", "", "", "");
        final NotificationEntity notification4 = new NotificationEntity(key, null, NotificationCategoryEnum.POLICY_VIOLATION, projectName, "", projectVersion, "", "", "", "", "");
        final NotificationEntity notification5 = new NotificationEntity(key, null, NotificationCategoryEnum.POLICY_VIOLATION_CLEARED, projectName, "", projectVersion, "", "", "", "", "");

        final NotificationModel model1 = new NotificationModel(notification1, createVulnerabilityList());
        final NotificationModel model2 = new NotificationModel(notification2, createVulnerabilityList());
        final NotificationModel model3 = new NotificationModel(notification3, createVulnerabilityList());
        final NotificationModel model4 = new NotificationModel(notification4, createVulnerabilityList());
        final NotificationModel model5 = new NotificationModel(notification5, createVulnerabilityList());

        return Arrays.asList(model1, model2, model1, model3, model2, model4, model5);
    }

    private List<NotificationModel> createVulnerabilityNotifications() {
        final String keyPrefix = "key";
        final String projectName = "Project Name";
        final String projectVersion = "Project Version";
        final NotificationEntity notification1 = new NotificationEntity(keyPrefix + "1", null, NotificationCategoryEnum.HIGH_VULNERABILITY, projectName, "", projectVersion, "", "", "", "", "");
        final NotificationEntity notification2 = new NotificationEntity(keyPrefix + "2", null, NotificationCategoryEnum.MEDIUM_VULNERABILITY, projectName, "", projectVersion, "", "", "", "", "");
        final NotificationEntity notification3 = new NotificationEntity(keyPrefix + "3", null, NotificationCategoryEnum.LOW_VULNERABILITY, projectName, "", projectVersion, "", "", "", "", "");
        final NotificationEntity notification4 = new NotificationEntity(keyPrefix + "4", null, NotificationCategoryEnum.VULNERABILITY, projectName, "", projectVersion, "", "", "", "", "");

        final NotificationModel model1 = new NotificationModel(notification1, createVulnerabilityList());
        final NotificationModel model2 = new NotificationModel(notification2, createVulnerabilityList());
        final NotificationModel model3 = new NotificationModel(notification3, createVulnerabilityList());
        final NotificationModel model4 = new NotificationModel(notification4, Collections.emptyList());

        return Arrays.asList(model1, model2, model1, model3, model2, model4);
    }

    private List<VulnerabilityEntity> createVulnerabilityList() {
        final VulnerabilityEntity vuln1 = new VulnerabilityEntity("id1", VulnerabilityOperationEnum.ADD, 1L);
        final VulnerabilityEntity vuln2 = new VulnerabilityEntity("id2", VulnerabilityOperationEnum.DELETE, 2L);
        final VulnerabilityEntity vuln3 = new VulnerabilityEntity("id3", VulnerabilityOperationEnum.UPDATE, 1L);

        return Arrays.asList(vuln1, vuln2, vuln1, vuln3, vuln2);
    }

}
