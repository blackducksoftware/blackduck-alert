package com.synopsys.integration.alert.workflow.upgrade;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.exception.AlertUpgradeException;

public class SemanticVersionTest {

    @Test
    public void versionComparisonTest() throws AlertUpgradeException {
        final SemanticVersion version = new SemanticVersion("1.0.0");
        final SemanticVersion versionSnapshot = new SemanticVersion("1.0.0-SNAPSHOT");
        final SemanticVersion higherVersion = new SemanticVersion("1.2.0");
        final SemanticVersion highestVersion = new SemanticVersion("2.1.0");
        final SemanticVersion lowVersion = new SemanticVersion("0.2.0");
        final SemanticVersion lowestVersion = new SemanticVersion("0.0.9999");

        assertTrue(version.compareTo(higherVersion) == SemanticVersion.CONSTANT_LESS_THAN);
        assertTrue(version.compareTo(highestVersion) == SemanticVersion.CONSTANT_LESS_THAN);
        assertTrue(version.compareTo(lowVersion) == SemanticVersion.CONSTANT_GREATER_THAN);
        assertTrue(version.compareTo(lowestVersion) == SemanticVersion.CONSTANT_GREATER_THAN);
        assertTrue(version.compareTo(version) == SemanticVersion.CONSTANT_EQUALS);
        assertTrue(version.compareTo(versionSnapshot) == SemanticVersion.CONSTANT_LESS_THAN);
    }
}
