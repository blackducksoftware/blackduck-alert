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

import com.blackducksoftware.integration.hub.dataservice.model.ProjectVersionModel;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.model.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.model.view.PolicyRuleView;
import com.blackducksoftware.integration.hub.model.view.components.MetaView;
import com.blackducksoftware.integration.hub.notification.processor.MapProcessorCache;
import com.blackducksoftware.integration.test.TestLogger;

public class PolicyViolationProcessorTest {

    @Test
    public void testProcess() throws HubIntegrationException, URISyntaxException {
        final MapProcessorCache cache = new MapProcessorCache();
        final PolicyViolationProcessor policyViolationProcessor = new PolicyViolationProcessor(cache, new TestLogger());

        final Date createdAt = new Date();
        final ProjectVersionModel projectVersionModel = new ProjectVersionModel();
        final String componentName = "Content item test";
        final ComponentVersionView componentVersionView = new ComponentVersionView();
        final String componentUrl = "url";
        final String componentVersionUrl = "newest";
        final PolicyRuleView policyRuleView = new PolicyRuleView();
        final MetaView metaView = new MetaView();
        metaView.href = "Meta href";
        policyRuleView.meta = metaView;
        final List<PolicyRuleView> policyRuleList = Arrays.asList(policyRuleView);
        final String coponentIssueUrl = "issueUrl";
        final PolicyViolationContentItem notification = new PolicyViolationContentItem(createdAt, projectVersionModel, componentName, componentVersionView, componentUrl, componentVersionUrl, policyRuleList, coponentIssueUrl);

        assertTrue(cache.getEvents().size() == 0);

        policyViolationProcessor.process(notification);

        assertTrue(cache.getEvents().size() == 1);
    }
}
