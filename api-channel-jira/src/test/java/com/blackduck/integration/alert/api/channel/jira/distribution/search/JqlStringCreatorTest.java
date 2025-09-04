/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.jira.distribution.search;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcernType;
import com.blackduck.integration.alert.common.message.model.LinkableItem;

class JqlStringCreatorTest {
    private static final String JIRA_PROJECT_KEY = "TEST";
    private final LinkableItem provider = new LinkableItem("Black Duck", "qa-hub10", "http://providerUrl");
    private final LinkableItem project = new LinkableItem(
        "Project",
        "project_with_single_quote'",
        "http://projectUrl/api/projects/project-uuid"
    );
    private final LinkableItem projectVersion = new LinkableItem(
        "Project Version",
        "v1.0 \\& v2.0",
        "http://projectVersionUrl/api/projects/project-uuid/versions/project-version-uuid/components"
    );
    private final LinkableItem component = new LinkableItem(
        "Component",
        "myVulnerableComponent",
        null
    );
    private final LinkableItem componentVersion = new LinkableItem(
        "Component Version",
        "1.1.2",
        "http://componentVersionUrl"
    );

    @Test
    void policySearchStringTest() {
        String policyName = "MyPolicy";
        String jqlString = JqlStringCreator.createBlackDuckComponentConcernIssuesSearchString(
            JIRA_PROJECT_KEY,
            provider,
            project,
            projectVersion,
            component,
            componentVersion,
            ComponentConcernType.POLICY,
            policyName
        );
        String expectedSearchString =
            "("
                + "project = 'TEST'"
                + " AND comment ~\"=== BEGIN JIRA ISSUE KEYS ===\""
                + " AND comment ~ \"provider: http://providerUrl\""
                + " AND comment ~ \"projectId: project_with_single_quote\\'\""
                + " AND comment ~ \"projectVersionId: v1.0 \\\\& v2.0\""
                + " AND comment ~ \"componentName: myVulnerableComponent\""
                + " AND comment ~ \"componentVersionName: 1.1.2\""
                + " AND comment ~ \"category: POLICY\""
                + " AND comment ~ \"policyName: Policy ViolatedMyPolicy\""
                + " ) OR ("
                + "project = 'TEST' "
                + "AND (issue.property[com-blackduck-integration-alert].provider = 'Black Duck' OR issue.property[com-synopsys-integration-alert].provider = 'Black Duck') "
                + "AND (issue.property[com-blackduck-integration-alert].providerUrl = 'http://providerUrl/' OR issue.property[com-synopsys-integration-alert].providerUrl = 'http://providerUrl/') "
                + "AND (issue.property[com-blackduck-integration-alert].topicName = 'Project' OR issue.property[com-synopsys-integration-alert].topicName = 'Project') "
                + "AND (issue.property[com-blackduck-integration-alert].topicValue = 'project_with_single_quote\\'' OR issue.property[com-synopsys-integration-alert].topicValue = 'project_with_single_quote\\'') "
                + "AND (issue.property[com-blackduck-integration-alert].subTopicName = 'Project Version' OR issue.property[com-synopsys-integration-alert].subTopicName = 'Project Version') "
                + "AND (issue.property[com-blackduck-integration-alert].subTopicValue = 'v1.0 \\\\& v2.0' OR issue.property[com-synopsys-integration-alert].subTopicValue = 'v1.0 \\\\& v2.0') "
                + "AND (issue.property[com-blackduck-integration-alert].componentName = 'Component' OR issue.property[com-synopsys-integration-alert].componentName = 'Component') "
                + "AND (issue.property[com-blackduck-integration-alert].componentValue = 'myVulnerableComponent' OR issue.property[com-synopsys-integration-alert].componentValue = 'myVulnerableComponent') "
                + "AND (issue.property[com-blackduck-integration-alert].subComponentName = 'Component Version' OR issue.property[com-synopsys-integration-alert].subComponentName = 'Component Version') "
                + "AND (issue.property[com-blackduck-integration-alert].subComponentValue = '1.1.2' OR issue.property[com-synopsys-integration-alert].subComponentValue = '1.1.2') "
                + "AND (issue.property[com-blackduck-integration-alert].category = 'Policy' OR issue.property[com-synopsys-integration-alert].category = 'Policy') "
                + "AND (issue.property[com-blackduck-integration-alert].additionalKey = 'Policy ViolatedMyPolicy' OR issue.property[com-synopsys-integration-alert].additionalKey = 'Policy ViolatedMyPolicy') "
                + ")";

        assertEquals(expectedSearchString, jqlString);
    }

    @Test
    void vulnerabilitySearchStringTest() {
        String jqlString = JqlStringCreator.createBlackDuckComponentConcernIssuesSearchString(
            JIRA_PROJECT_KEY,
            provider,
            project,
            projectVersion,
            component,
            componentVersion,
            ComponentConcernType.VULNERABILITY,
            null
        );
        String expectedSearchString =
            "("
                + "project = 'TEST'"
                + " AND comment ~\"=== BEGIN JIRA ISSUE KEYS ===\""
                + " AND comment ~ \"provider: http://providerUrl\""
                + " AND comment ~ \"projectId: project_with_single_quote\\'\""
                + " AND comment ~ \"projectVersionId: v1.0 \\\\& v2.0\""
                + " AND comment ~ \"componentName: myVulnerableComponent\""
                + " AND comment ~ \"componentVersionName: 1.1.2\""
                + " AND comment ~ \"category: VULNERABILITY\""
                + " ) OR ("
                + "project = 'TEST' "
                + "AND (issue.property[com-blackduck-integration-alert].provider = 'Black Duck' OR issue.property[com-synopsys-integration-alert].provider = 'Black Duck') "
                + "AND (issue.property[com-blackduck-integration-alert].providerUrl = 'http://providerUrl/' OR issue.property[com-synopsys-integration-alert].providerUrl = 'http://providerUrl/') "
                + "AND (issue.property[com-blackduck-integration-alert].topicName = 'Project' OR issue.property[com-synopsys-integration-alert].topicName = 'Project') "
                + "AND (issue.property[com-blackduck-integration-alert].topicValue = 'project_with_single_quote\\'' OR issue.property[com-synopsys-integration-alert].topicValue = 'project_with_single_quote\\'') "
                + "AND (issue.property[com-blackduck-integration-alert].subTopicName = 'Project Version' OR issue.property[com-synopsys-integration-alert].subTopicName = 'Project Version') "
                + "AND (issue.property[com-blackduck-integration-alert].subTopicValue = 'v1.0 \\\\& v2.0' OR issue.property[com-synopsys-integration-alert].subTopicValue = 'v1.0 \\\\& v2.0') "
                + "AND (issue.property[com-blackduck-integration-alert].componentName = 'Component' OR issue.property[com-synopsys-integration-alert].componentName = 'Component') "
                + "AND (issue.property[com-blackduck-integration-alert].componentValue = 'myVulnerableComponent' OR issue.property[com-synopsys-integration-alert].componentValue = 'myVulnerableComponent') "
                + "AND (issue.property[com-blackduck-integration-alert].subComponentName = 'Component Version' OR issue.property[com-synopsys-integration-alert].subComponentName = 'Component Version') "
                + "AND (issue.property[com-blackduck-integration-alert].subComponentValue = '1.1.2' OR issue.property[com-synopsys-integration-alert].subComponentValue = '1.1.2') "
                + "AND (issue.property[com-blackduck-integration-alert].category = 'Vulnerability' OR issue.property[com-synopsys-integration-alert].category = 'Vulnerability') "
                + ")";

        assertEquals(expectedSearchString, jqlString);
    }

    @Test
    void injectionTest() {
        String exploitableString = "INJECTED\\' OR project = \"SomeOtherProject\" OR summary ~ 2021-01-01\\\\\\";
        LinkableItem projectVersionInjected = new LinkableItem(
            "Project Version",
            exploitableString,
            "http:///api/projects/project-uuid/versions/projectVersionUrl"
        );
        String jqlString = JqlStringCreator.createBlackDuckComponentConcernIssuesSearchString(
            JIRA_PROJECT_KEY,
            provider,
            project,
            projectVersionInjected,
            component,
            componentVersion,
            ComponentConcernType.VULNERABILITY,
            null
        );
        String expectedSearchString =
            "("
                + "project = 'TEST'"
                + " AND comment ~\"=== BEGIN JIRA ISSUE KEYS ===\""
                + " AND comment ~ \"provider: http://providerUrl\""
                + " AND comment ~ \"projectId: project_with_single_quote\\'\""
                + " AND comment ~ \"projectVersionId: INJECTED\\\\\\' OR project = \"SomeOtherProject\" OR summary ~ 2021-01-01\\\\\\\\\\\\\""
                + " AND comment ~ \"componentName: myVulnerableComponent\""
                + " AND comment ~ \"componentVersionName: 1.1.2\""
                + " AND comment ~ \"category: VULNERABILITY\""
                + " ) OR ("
                + "project = 'TEST' "
                + "AND (issue.property[com-blackduck-integration-alert].provider = 'Black Duck' OR issue.property[com-synopsys-integration-alert].provider = 'Black Duck') "
                + "AND (issue.property[com-blackduck-integration-alert].providerUrl = 'http://providerUrl/' OR issue.property[com-synopsys-integration-alert].providerUrl = 'http://providerUrl/') "
                + "AND (issue.property[com-blackduck-integration-alert].topicName = 'Project' OR issue.property[com-synopsys-integration-alert].topicName = 'Project') "
                + "AND (issue.property[com-blackduck-integration-alert].topicValue = 'project_with_single_quote\\'' OR issue.property[com-synopsys-integration-alert].topicValue = 'project_with_single_quote\\'') "
                + "AND (issue.property[com-blackduck-integration-alert].subTopicName = 'Project Version' OR issue.property[com-synopsys-integration-alert].subTopicName = 'Project Version') "
                + "AND (issue.property[com-blackduck-integration-alert].subTopicValue = 'INJECTED\\\\\\' OR project = \"SomeOtherProject\" OR summary ~ 2021-01-01\\\\\\\\\\\\' OR issue.property[com-synopsys-integration-alert].subTopicValue = 'INJECTED\\\\\\' OR project = \"SomeOtherProject\" OR summary ~ 2021-01-01\\\\\\\\\\\\') "
                + "AND (issue.property[com-blackduck-integration-alert].componentName = 'Component' OR issue.property[com-synopsys-integration-alert].componentName = 'Component') "
                + "AND (issue.property[com-blackduck-integration-alert].componentValue = 'myVulnerableComponent' OR issue.property[com-synopsys-integration-alert].componentValue = 'myVulnerableComponent') "
                + "AND (issue.property[com-blackduck-integration-alert].subComponentName = 'Component Version' OR issue.property[com-synopsys-integration-alert].subComponentName = 'Component Version') "
                + "AND (issue.property[com-blackduck-integration-alert].subComponentValue = '1.1.2' OR issue.property[com-synopsys-integration-alert].subComponentValue = '1.1.2') "
                + "AND (issue.property[com-blackduck-integration-alert].category = 'Vulnerability' OR issue.property[com-synopsys-integration-alert].category = 'Vulnerability') "
                + ")";

        assertEquals(expectedSearchString, jqlString);
    }
}
