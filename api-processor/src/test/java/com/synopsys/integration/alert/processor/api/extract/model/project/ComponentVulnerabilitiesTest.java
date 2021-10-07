package com.synopsys.integration.alert.processor.api.extract.model.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.message.model.LinkableItem;

public class ComponentVulnerabilitiesTest {
    private static final LinkableItem CRITICAL = new LinkableItem("Vulnerability", ComponentConcernSeverity.CRITICAL.getVulnerabilityLabel());
    private static final LinkableItem HIGH = new LinkableItem("Vulnerability", ComponentConcernSeverity.CRITICAL.getVulnerabilityLabel());
    private static final LinkableItem MEDIUM = new LinkableItem("Vulnerability", ComponentConcernSeverity.CRITICAL.getVulnerabilityLabel());
    private static final LinkableItem LOW = new LinkableItem("Vulnerability", ComponentConcernSeverity.CRITICAL.getVulnerabilityLabel());

    @Test
    public void hasVulnerabilitiesTest() {
        ComponentVulnerabilities componentVulnerabilities = new ComponentVulnerabilities(List.of(CRITICAL), List.of(HIGH), List.of(MEDIUM), List.of(LOW));
        assertTrue(componentVulnerabilities.hasVulnerabilities());

        ComponentVulnerabilities componentVulnerabilitiesNone = ComponentVulnerabilities.none();
        assertFalse(componentVulnerabilitiesNone.hasVulnerabilities());
    }

    @Test
    public void computeHighestSeverityNoneTest() {
        ComponentVulnerabilities componentVulnerabilities = ComponentVulnerabilities.none();
        Optional<ComponentConcernSeverity> severity = componentVulnerabilities.computeHighestSeverity();
        assertFalse(severity.isPresent());
    }

    @Test
    public void computeHighestSeverityCritical() {
        ComponentVulnerabilities componentVulnerabilities = new ComponentVulnerabilities(List.of(CRITICAL), List.of(HIGH), List.of(MEDIUM), List.of(LOW));
        Optional<ComponentConcernSeverity> severity = componentVulnerabilities.computeHighestSeverity();
        assertTrue(severity.isPresent());
        assertEquals(ComponentConcernSeverity.CRITICAL, severity.get());
    }

    @Test
    public void computeHighestSeverityHigh() {
        ComponentVulnerabilities componentVulnerabilities = new ComponentVulnerabilities(List.of(), List.of(HIGH), List.of(MEDIUM), List.of(LOW));
        Optional<ComponentConcernSeverity> severity = componentVulnerabilities.computeHighestSeverity();
        assertTrue(severity.isPresent());
        assertEquals(ComponentConcernSeverity.MAJOR_HIGH, severity.get());
    }

    @Test
    public void computeHighestSeverityMedium() {
        ComponentVulnerabilities componentVulnerabilities = new ComponentVulnerabilities(List.of(), List.of(), List.of(MEDIUM), List.of(LOW));
        Optional<ComponentConcernSeverity> severity = componentVulnerabilities.computeHighestSeverity();
        assertTrue(severity.isPresent());
        assertEquals(ComponentConcernSeverity.MINOR_MEDIUM, severity.get());
    }

    @Test
    public void computeHighestSeverityLow() {
        ComponentVulnerabilities componentVulnerabilities = new ComponentVulnerabilities(List.of(), List.of(), List.of(), List.of(LOW));
        Optional<ComponentConcernSeverity> severity = componentVulnerabilities.computeHighestSeverity();
        assertTrue(severity.isPresent());
        assertEquals(ComponentConcernSeverity.TRIVIAL_LOW, severity.get());
    }

}
