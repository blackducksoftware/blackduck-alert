package com.synopsys.integration.alert.workflow.upgrade;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.exception.AlertUpgradeException;

public class SemanticVersionTest {

    @Test
    public void versionComparisonTest() throws AlertUpgradeException {
        final SemanticVersion version = new SemanticVersion("1.0.1");
        final SemanticVersion versionSnapshot = new SemanticVersion("1.0.1-SNAPSHOT");
        final SemanticVersion higherVersion = new SemanticVersion("1.2.0");
        final SemanticVersion highestVersion = new SemanticVersion("2.1.0");
        final SemanticVersion lowVersion = new SemanticVersion("0.2.0");
        final SemanticVersion lowestVersion = new SemanticVersion("0.0.9999");

        assertEquals(SemanticVersion.LESS_THAN, version.compareTo(higherVersion));
        assertEquals(SemanticVersion.LESS_THAN, version.compareTo(highestVersion));
        assertEquals(SemanticVersion.GREATER_THAN, version.compareTo(lowVersion));
        assertEquals(SemanticVersion.GREATER_THAN, version.compareTo(lowestVersion));
        assertEquals(SemanticVersion.EQUALS, version.compareTo(version));
        assertEquals(SemanticVersion.GREATER_THAN, version.compareTo(versionSnapshot));
    }
}
