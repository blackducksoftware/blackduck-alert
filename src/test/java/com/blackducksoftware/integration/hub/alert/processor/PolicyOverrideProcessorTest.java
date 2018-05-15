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
package com.blackducksoftware.integration.hub.alert.processor;

import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.throwaway.PolicyOverrideProcessor;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.PolicyRuleView;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.throwaway.MapProcessorCache;
import com.blackducksoftware.integration.hub.throwaway.PolicyOverrideContentItem;
import com.blackducksoftware.integration.hub.throwaway.ProjectVersionModel;
import com.blackducksoftware.integration.test.TestLogger;

public class PolicyOverrideProcessorTest {

    @Test
    public void testProcessRemoveEvent() throws HubIntegrationException, URISyntaxException {
        // Test will break if ListProcessorCache is used because NotificationEvents does not have an equals method defined
        final MapProcessorCache cache = new MapProcessorCache();
        final PolicyOverrideProcessor policyOverrideProcessor = new PolicyOverrideProcessor(cache, new TestLogger());

        final Date createdAt = new Date();
        final ProjectVersionModel projectVersionModel = new ProjectVersionModel();
        final String componentName = "Content item test";
        final ComponentVersionView componentVersionView = new ComponentVersionView();
        final String componentUrl = "google.com";
        final String componentVersionUrl = "newest";
        final PolicyRuleView policyRuleView = new PolicyRuleView();
        final List<PolicyRuleView> policyRuleList = Arrays.asList(policyRuleView);
        final String firstName = "B";
        final String lastName = "Dawg";
        final String componentIssueUrl = "broken.edu";
        final PolicyOverrideContentItem notification = new PolicyOverrideContentItem(createdAt, projectVersionModel, componentName, componentVersionView, componentUrl, componentVersionUrl, policyRuleList, firstName, lastName,
                componentIssueUrl);

        assertTrue(cache.getEvents().size() == 0);

        final PolicyOverrideProcessor spyProcessor = Mockito.spy(policyOverrideProcessor);
        Mockito.doReturn("key").when(spyProcessor).generateEventKey(Mockito.anyMap());

        spyProcessor.process(notification);

        assertTrue(cache.getEvents().size() == 1);

        spyProcessor.process(notification);

        assertTrue(cache.getEvents().size() == 0);
    }
}
