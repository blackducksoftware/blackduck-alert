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

import com.blackducksoftware.integration.hub.api.generated.component.ResourceMetadata;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.PolicyRuleView;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.notification.MapProcessorCache;
import com.blackducksoftware.integration.hub.notification.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.notification.ProjectVersionModel;
import com.blackducksoftware.integration.test.TestLogger;

public class PolicyViolationProcessorTest {

    @Test
    public void testProcess() throws HubIntegrationException, URISyntaxException {
        final Date createdAt = new Date();
        final ProjectVersionModel projectVersionModel = new ProjectVersionModel();
        final String componentName = "Content item test";
        final ComponentVersionView componentVersionView = new ComponentVersionView();
        final String componentUrl = "url";
        final String componentVersionUrl = "newest";
        final PolicyRuleView policyRuleView = new PolicyRuleView();
        final ResourceMetadata metaView = new ResourceMetadata();
        metaView.href = "Meta href";
        policyRuleView._meta = metaView;
        final List<PolicyRuleView> policyRuleList = Arrays.asList(policyRuleView);
        final String componentIssueUrl = "issueUrl";
        final PolicyViolationContentItem notification = new PolicyViolationContentItem(createdAt, projectVersionModel, componentName, componentVersionView, componentUrl, componentVersionUrl, policyRuleList, componentIssueUrl);

        processTestRun(notification);
    }

    @Test
    public void testProcessWithoutVersion() throws URISyntaxException, HubIntegrationException {
        final Date createdAt = new Date();
        final ProjectVersionModel projectVersionModel = new ProjectVersionModel();
        final String componentName = "Content item test";
        final String componentUrl = "url";
        final String componentVersionUrl = "newest";
        final PolicyRuleView policyRuleView = new PolicyRuleView();
        final ResourceMetadata metaView = new ResourceMetadata();
        metaView.href = "Meta href";
        policyRuleView._meta = metaView;
        final List<PolicyRuleView> policyRuleList = Arrays.asList(policyRuleView);
        final String componentIssueUrl = "issueUrl";
        final PolicyViolationContentItem notification = new PolicyViolationContentItem(createdAt, projectVersionModel, componentName, null, componentUrl, componentVersionUrl, policyRuleList, componentIssueUrl);

        processTestRun(notification);
    }

    private void processTestRun(final PolicyViolationContentItem contentItem) throws HubIntegrationException {
        final MapProcessorCache cache = new MapProcessorCache();
        final PolicyViolationProcessor policyViolationProcessor = new PolicyViolationProcessor(cache, new TestLogger());

        assertTrue(cache.getEvents().size() == 0);

        policyViolationProcessor.process(contentItem);

        assertTrue(cache.getEvents().size() == 1);
    }
}
