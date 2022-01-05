package com.synopsys.integration.alert.api.channel.jira.distribution.custom;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class MessageValueReplacementResolverTest {
    @Test
    public void replacementFieldValueTest() {
        String originalFieldValue = "ProjectName: {{projectName}} | ProviderName: {{providerName}} | ProjectVersionName: {{projectVersion}} | ComponentName: {{componentName}} | ComponentVersionName: {{componentVersion}}";
        String expectedFieldValue = "ProjectName: ProjectNameREPLACED | ProviderName: ProviderNameREPLACED | ProjectVersionName: ProjectVersionNameREPLACED | ComponentName: componentNameReplaced | ComponentVersionName: componentVersionNameReplaced";

        MessageReplacementValues messageReplacementValues = new MessageReplacementValues.Builder("ProviderNameREPLACED", "ProjectNameREPLACED")
                                                                .projectVersionName("ProjectVersionNameREPLACED")
                                                                .componentName("componentNameReplaced")
                                                                .componentVersionName("componentVersionNameReplaced")
                                                                .build();
        testReplacements(messageReplacementValues, originalFieldValue, expectedFieldValue);
    }

    @Test
    public void replacementFieldValueNullTest() {
        String originalFieldValue = "ProjectName: {{projectName}}, ProjectVersion: {{projectVersion}}, ComponentName: {{componentName}}, ComponentVersion: {{componentVersion}}";
        String expectedFieldValue = "ProjectName: ProjectNameREPLACED, ProjectVersion: None, ComponentName: None, ComponentVersion: None";

        MessageReplacementValues messageReplacementValues = new MessageReplacementValues.Builder("ProviderNameREPLACED", "ProjectNameREPLACED")
                                                                .build();
        testReplacements(messageReplacementValues, originalFieldValue, expectedFieldValue);
    }

    @Test
    public void doubleReplacementTest() {
        String originalFieldValue = "ProjectName: {{projectName}} | ProjectName2: {{projectName}}";
        String expectedFieldValue = "ProjectName: ProjectNameREPLACED | ProjectName2: ProjectNameREPLACED";
        MessageReplacementValues messageReplacementValues = new MessageReplacementValues.Builder("ProviderNameREPLACED", "ProjectNameREPLACED")
                                                                .build();

        testReplacements(messageReplacementValues, originalFieldValue, expectedFieldValue);
    }

    @Test
    public void badReplacementValueTest() {
        String originalFieldValue = "ProjectName: {{notAValidReplacement}}";
        MessageReplacementValues messageReplacementValues = new MessageReplacementValues.Builder("ProviderNameREPLACED", "ProjectNameREPLACED")
                                                                .build();

        testReplacements(messageReplacementValues, originalFieldValue, originalFieldValue);
    }

    @Test
    public void missingBuilderReplacementTest() {
        String originalFieldValue = "Missing from builder: {{severity}}";
        String expectedFieldValue = "Missing from builder: None";
        MessageReplacementValues messageReplacementValues = new MessageReplacementValues.Builder("ProviderNameREPLACED", "ProjectNameREPLACED")
                                                                .build();

        testReplacements(messageReplacementValues, originalFieldValue, expectedFieldValue);
    }

    @Test
    public void replacementSeverityTest() {
        String originalFieldValue = "Severity: {{severity}}";
        String expectedFieldValue = "Severity: CRITICAL";
        MessageReplacementValues messageReplacementValues = new MessageReplacementValues.Builder("ProviderNameREPLACED", "ProjectNameREPLACED")
                                                                .severity("CRITICAL")
                                                                .build();

        testReplacements(messageReplacementValues, originalFieldValue, expectedFieldValue);
    }

    @Test
    public void replacementPolicyCategoryTest() {
        String originalFieldValue = "Policy Category: {{policyCategory}}";
        String expectedFieldValue = "Policy Category: UNCATEGORIZED";
        MessageReplacementValues messageReplacementValues = new MessageReplacementValues.Builder("ProviderNameREPLACED", "ProjectNameREPLACED")
                                                                .policyCategory("UNCATEGORIZED")
                                                                .build();

        testReplacements(messageReplacementValues, originalFieldValue, expectedFieldValue);
    }

    @Test
    public void replacementComponentUsageTest() {
        String originalFieldValue = "Component Usage: {{componentUsage}}";
        String expectedFieldValue = "Component Usage: Dynamically Linked";
        MessageReplacementValues messageReplacementValues = new MessageReplacementValues.Builder("ProviderNameREPLACED", "ProjectNameREPLACED")
                                                                .componentUsage("Dynamically Linked")
                                                                .build();

        testReplacements(messageReplacementValues, originalFieldValue, expectedFieldValue);
    }

    @Test
    public void replacementComponentLicenseTest() {
        String originalFieldValue = "Component License: {{componentLicense}}";
        String expectedFieldValue = "Component License: GPL-2.0";
        MessageReplacementValues messageReplacementValues = new MessageReplacementValues.Builder("ProviderNameREPLACED", "ProjectNameREPLACED")
                                                                .componentLicense("GPL-2.0")
                                                                .build();

        testReplacements(messageReplacementValues, originalFieldValue, expectedFieldValue);
    }

    @Test
    public void replacementUpgradeGuidanceTest() {
        String originalFieldValue = "Short Term Guidance: {{shortTermUpgradeGuidance}} | Long Term Guidance {{longTermUpgradeGuidance}}";
        String expectedFieldValue = "Short Term Guidance: v1.0 | Long Term Guidance v2.0";
        MessageReplacementValues messageReplacementValues = new MessageReplacementValues.Builder("ProviderNameREPLACED", "ProjectNameREPLACED")
                                                                .shortTermUpgradeGuidance("v1.0")
                                                                .longTermUpgradeGuidance("v2.0")
                                                                .build();

        testReplacements(messageReplacementValues, originalFieldValue, expectedFieldValue);
    }

    @Test
    public void replacementProviderTypeAndName() {
        String originalFieldValue = "Old providerName: {{providerName}} | new providerType: {{providerType}} (Should be the same)";
        String expectedFieldValue = "Old providerName: BlackDuck | new providerType: BlackDuck (Should be the same)";

        MessageReplacementValues messageReplacementValues = new MessageReplacementValues.Builder("BlackDuck", "Project name").build();
        testReplacements(messageReplacementValues, originalFieldValue, expectedFieldValue);
    }

    private void testReplacements(MessageReplacementValues messageReplacementValues, String originalValue, String expectedValue) {
        MessageValueReplacementResolver messageValueReplacementResolver = new MessageValueReplacementResolver(messageReplacementValues);
        String replacedFieldValue = messageValueReplacementResolver.createReplacedFieldValue(originalValue);

        assertEquals(expectedValue, replacedFieldValue);
    }
}
