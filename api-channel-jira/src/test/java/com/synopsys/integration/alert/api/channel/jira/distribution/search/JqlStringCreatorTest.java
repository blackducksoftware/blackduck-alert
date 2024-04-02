package com.synopsys.integration.alert.api.channel.jira.distribution.search;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.api.processor.extract.model.project.ComponentConcernType;

class JqlStringCreatorTest {
    private static final String JIRA_PROJECT_KEY = "TEST";
    private final LinkableItem provider = new LinkableItem("Black Duck", "qa-hub10", "http://providerUrl");
    private final LinkableItem project = new LinkableItem(
        "Project",
        "project_with_single_quote'",
        "http://projectUrl"
    );
    private final LinkableItem projectVersion = new LinkableItem(
        "Project Version",
        "v1.0 \\& v2.0",
        "http://projectVersionUrl"
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
        String expectedSearchString = "project = 'TEST' AND issue.property[com-synopsys-integration-alert].provider = 'Black Duck' "
            + "AND issue.property[com-synopsys-integration-alert].providerUrl = 'http://providerUrl/' "
            + "AND issue.property[com-synopsys-integration-alert].topicName = 'Project' "
            + "AND issue.property[com-synopsys-integration-alert].topicValue = 'project_with_single_quote\\'' "
            + "AND issue.property[com-synopsys-integration-alert].subTopicName = 'Project Version' "
            + "AND issue.property[com-synopsys-integration-alert].subTopicValue = 'v1.0 \\\\& v2.0' "
            + "AND issue.property[com-synopsys-integration-alert].componentName = 'Component' "
            + "AND issue.property[com-synopsys-integration-alert].componentValue = 'myVulnerableComponent' "
            + "AND issue.property[com-synopsys-integration-alert].subComponentName = 'Component Version' "
            + "AND issue.property[com-synopsys-integration-alert].subComponentValue = '1.1.2' "
            + "AND issue.property[com-synopsys-integration-alert].category = 'Policy' "
            + "AND issue.property[com-synopsys-integration-alert].additionalKey = 'Policy ViolatedMyPolicy' ";

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
        String expectedSearchString = "project = 'TEST' AND issue.property[com-synopsys-integration-alert].provider = 'Black Duck' "
            + "AND issue.property[com-synopsys-integration-alert].providerUrl = 'http://providerUrl/' "
            + "AND issue.property[com-synopsys-integration-alert].topicName = 'Project' "
            + "AND issue.property[com-synopsys-integration-alert].topicValue = 'project_with_single_quote\\'' "
            + "AND issue.property[com-synopsys-integration-alert].subTopicName = 'Project Version' "
            + "AND issue.property[com-synopsys-integration-alert].subTopicValue = 'v1.0 \\\\& v2.0' "
            + "AND issue.property[com-synopsys-integration-alert].componentName = 'Component' "
            + "AND issue.property[com-synopsys-integration-alert].componentValue = 'myVulnerableComponent' "
            + "AND issue.property[com-synopsys-integration-alert].subComponentName = 'Component Version' "
            + "AND issue.property[com-synopsys-integration-alert].subComponentValue = '1.1.2' "
            + "AND issue.property[com-synopsys-integration-alert].category = 'Vulnerability' ";

        assertEquals(expectedSearchString, jqlString);
    }

    @Test
    void injectionTest() {
        String exploitableString = "INJECTED\\' OR project = \"SomeOtherProject\" OR summary ~ 2021-01-01\\\\\\";
        LinkableItem projectVersionInjected = new LinkableItem(
            "Project Version",
            exploitableString,
            "http://projectVersionUrl"
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
        String expectedSearchString = "project = 'TEST' AND issue.property[com-synopsys-integration-alert].provider = 'Black Duck' "
            + "AND issue.property[com-synopsys-integration-alert].providerUrl = 'http://providerUrl/' "
            + "AND issue.property[com-synopsys-integration-alert].topicName = 'Project' "
            + "AND issue.property[com-synopsys-integration-alert].topicValue = 'project_with_single_quote\\'' "
            + "AND issue.property[com-synopsys-integration-alert].subTopicName = 'Project Version' "
            + "AND issue.property[com-synopsys-integration-alert].subTopicValue = 'INJECTED\\\\\\' OR project = \"SomeOtherProject\" OR summary ~ 2021-01-01\\\\\\\\\\\\' "
            + "AND issue.property[com-synopsys-integration-alert].componentName = 'Component' "
            + "AND issue.property[com-synopsys-integration-alert].componentValue = 'myVulnerableComponent' "
            + "AND issue.property[com-synopsys-integration-alert].subComponentName = 'Component Version' "
            + "AND issue.property[com-synopsys-integration-alert].subComponentValue = '1.1.2' "
            + "AND issue.property[com-synopsys-integration-alert].category = 'Vulnerability' ";

        assertEquals(expectedSearchString, jqlString);
    }
}
