package com.synopsys.integration.alert.processor.api.summarize;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.ProcessedProviderMessage;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernSeverity;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentPolicy;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentUpgradeGuidance;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentVulnerabilities;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectOperation;

public class ProjectMessageSummarizerTest {
    private final LinkableItem commonProject = new LinkableItem("Project", "Common Project");
    private final LinkableItem commonProjectVersion = new LinkableItem("ProjectVersion", "Common Project Version");
    private final LinkableItem provider = new LinkableItem("Provider", "Provider");
    private final ProviderDetails providerDetails = new ProviderDetails(10L, provider);
    private final LinkableItem component = new LinkableItem("Component", "The component");
    private final LinkableItem componentVersion = new LinkableItem("Component Version", "The component version");

    private final ProjectMessageSummarizer projectMessageSummarizer = new ProjectMessageSummarizer();

    @Test
    public void summarizeProjectStatusTest() {
        ProjectOperation commonOperation = ProjectOperation.CREATE;
        ProjectMessage projectMessage = ProjectMessage.projectStatusInfo(providerDetails, commonProject, commonOperation);
        ProcessedProviderMessage<ProjectMessage> processedProviderMessage = new ProcessedProviderMessage<>(Set.of(1L), projectMessage);

        ProcessedProviderMessage<SimpleMessage> summarizedSimpleMessage = projectMessageSummarizer.summarize(processedProviderMessage);
        SimpleMessage simpleMessage = summarizedSimpleMessage.getProviderMessage();
        printSimpleMessage(simpleMessage);

        testProjectStatus(simpleMessage);
        assertEquals(1, simpleMessage.getDetails().size());
        assertTrue(simpleMessage.getDetails().contains(commonProject));
        assertTrue(simpleMessage.getSummary().contains(ProjectMessageSummarizer.OP_PARTICIPLE_CREATED));
    }

    @Test
    public void summarizeProjectStatusDeletedTest() {
        ProjectOperation commonOperation = ProjectOperation.DELETE;
        ProjectMessage projectMessage = ProjectMessage.projectStatusInfo(providerDetails, commonProject, commonOperation);
        ProcessedProviderMessage<ProjectMessage> processedProviderMessage = new ProcessedProviderMessage<>(Set.of(1L), projectMessage);

        ProcessedProviderMessage<SimpleMessage> summarizedSimpleMessage = projectMessageSummarizer.summarize(processedProviderMessage);
        SimpleMessage simpleMessage = summarizedSimpleMessage.getProviderMessage();
        printSimpleMessage(simpleMessage);

        testProjectStatus(simpleMessage);
        assertEquals(1, simpleMessage.getDetails().size());
        assertTrue(simpleMessage.getDetails().contains(commonProject));
        assertTrue(simpleMessage.getSummary().contains(ProjectMessageSummarizer.OP_PARTICIPLE_DELETED));
    }

    @Test
    public void summarizeProjectVersionStatusTest() {
        ProjectOperation commonOperation = ProjectOperation.CREATE;
        ProjectMessage projectMessage = ProjectMessage.projectVersionStatusInfo(providerDetails, commonProject, commonProjectVersion, commonOperation);
        ProcessedProviderMessage<ProjectMessage> processedProviderMessage = new ProcessedProviderMessage<>(Set.of(1L), projectMessage);

        ProcessedProviderMessage<SimpleMessage> summarizedSimpleMessage = projectMessageSummarizer.summarize(processedProviderMessage);
        SimpleMessage simpleMessage = summarizedSimpleMessage.getProviderMessage();
        printSimpleMessage(simpleMessage);

        testProjectStatus(simpleMessage);
        assertEquals(2, simpleMessage.getDetails().size(), "Expected 2 LinkableItems. One for project the other for project-version.");
        assertTrue(simpleMessage.getDetails().contains(commonProject));
        assertTrue(simpleMessage.getDetails().contains(commonProjectVersion));

        assertTrue(simpleMessage.getSummary().contains(commonProjectVersion.getValue()));
        assertTrue(simpleMessage.getSummary().contains(ProjectMessageSummarizer.OP_PARTICIPLE_CREATED));
        assertTrue(simpleMessage.getDescription().contains(commonProjectVersion.getValue()));
    }

    @Test
    public void summarizeComponentUpdateTest() {
        ComponentConcern policyConcern = ComponentConcern.severePolicy(ItemOperation.ADD, "A severe policy", ComponentConcernSeverity.TRIVIAL_LOW, "https://severe-policy");
        ComponentConcern vulnerabilityConcern1 = ComponentConcern.vulnerability(ItemOperation.ADD, "CVE-123", ComponentConcernSeverity.CRITICAL, "https://vuln-rul");
        ComponentConcern vulnerabilityConcern2 = ComponentConcern.vulnerability(ItemOperation.UPDATE, "CVE-135", ComponentConcernSeverity.TRIVIAL_LOW, "https://vuln-rul");
        ComponentConcern vulnerabilityConcern3 = ComponentConcern.vulnerability(ItemOperation.DELETE, "CVE-246", ComponentConcernSeverity.MINOR_MEDIUM, "https://vuln-rul");

        BomComponentDetails bomComponentDetails = createBomComponentDetails(List.of(policyConcern, vulnerabilityConcern1, vulnerabilityConcern2, vulnerabilityConcern3));

        ProjectMessage projectMessage = ProjectMessage.componentUpdate(providerDetails, commonProject, commonProjectVersion, List.of(bomComponentDetails));
        ProcessedProviderMessage<ProjectMessage> processedProviderMessage = new ProcessedProviderMessage<>(Set.of(1L), projectMessage);

        ProcessedProviderMessage<SimpleMessage> summarizedSimpleMessage = projectMessageSummarizer.summarize(processedProviderMessage);
        SimpleMessage simpleMessage = summarizedSimpleMessage.getProviderMessage();
        printSimpleMessage(simpleMessage);

        testProjectStatus(simpleMessage);
        testComponentStatus(simpleMessage);

        assertEquals(6, simpleMessage.getDetails().size());
        assertTrue(doesLabelExist(simpleMessage.getDetails(), policyConcern.getSeverity().getPolicyLabel(), "Policies", ProjectMessageSummarizer.OP_PARTICIPLE_VIOLATED));
        assertTrue(doesLabelExist(simpleMessage.getDetails(), vulnerabilityConcern1.getSeverity().getVulnerabilityLabel(), "Vulnerabilities", ProjectMessageSummarizer.OP_PARTICIPLE_ADDED));
        assertTrue(doesLabelExist(simpleMessage.getDetails(), vulnerabilityConcern2.getSeverity().getVulnerabilityLabel(), "Vulnerabilities", ProjectMessageSummarizer.OP_PARTICIPLE_UPDATED));
        assertTrue(doesLabelExist(simpleMessage.getDetails(), vulnerabilityConcern3.getSeverity().getVulnerabilityLabel(), "Vulnerabilities", ProjectMessageSummarizer.OP_PARTICIPLE_DELETED));

        assertTrue(simpleMessage.getDescription().contains("updates"));
    }

    @Test
    public void summarizeComponentConcernTest() {
        ComponentConcern policyConcern = ComponentConcern.severePolicy(ItemOperation.ADD, "A severe policy", ComponentConcernSeverity.TRIVIAL_LOW, "https://severe-policy");
        ComponentConcern vulnerabilityConcern1 = ComponentConcern.vulnerability(ItemOperation.ADD, "CVE-123", ComponentConcernSeverity.CRITICAL, "https://vuln-rul");
        ComponentConcern vulnerabilityConcern2 = ComponentConcern.vulnerability(ItemOperation.UPDATE, "CVE-135", ComponentConcernSeverity.TRIVIAL_LOW, "https://vuln-rul");
        ComponentConcern vulnerabilityConcern3 = ComponentConcern.vulnerability(ItemOperation.DELETE, "CVE-246", ComponentConcernSeverity.MINOR_MEDIUM, "https://vuln-rul");

        BomComponentDetails bomComponentDetails = createBomComponentDetails(List.of(policyConcern, vulnerabilityConcern1, vulnerabilityConcern2, vulnerabilityConcern3));

        ProjectMessage projectMessage = ProjectMessage.componentConcern(providerDetails, commonProject, commonProjectVersion, List.of(bomComponentDetails));
        ProcessedProviderMessage<ProjectMessage> processedProviderMessage = new ProcessedProviderMessage<>(Set.of(1L), projectMessage);

        ProcessedProviderMessage<SimpleMessage> summarizedSimpleMessage = projectMessageSummarizer.summarize(processedProviderMessage);
        SimpleMessage simpleMessage = summarizedSimpleMessage.getProviderMessage();
        printSimpleMessage(simpleMessage);

        testProjectStatus(simpleMessage);
        testComponentStatus(simpleMessage);

        assertEquals(6, simpleMessage.getDetails().size());
        assertTrue(doesLabelExist(simpleMessage.getDetails(), policyConcern.getSeverity().getPolicyLabel(), "Policies", ProjectMessageSummarizer.OP_PARTICIPLE_VIOLATED));
        assertTrue(doesLabelExist(simpleMessage.getDetails(), vulnerabilityConcern1.getSeverity().getVulnerabilityLabel(), "Vulnerabilities", ProjectMessageSummarizer.OP_PARTICIPLE_ADDED));
        assertTrue(doesLabelExist(simpleMessage.getDetails(), vulnerabilityConcern2.getSeverity().getVulnerabilityLabel(), "Vulnerabilities", ProjectMessageSummarizer.OP_PARTICIPLE_UPDATED));
        assertTrue(doesLabelExist(simpleMessage.getDetails(), vulnerabilityConcern3.getSeverity().getVulnerabilityLabel(), "Vulnerabilities", ProjectMessageSummarizer.OP_PARTICIPLE_DELETED));

        assertTrue(simpleMessage.getDescription().contains("problems"));
    }

    @Test
    public void groupedConcernCountTest() {
        ComponentConcern vulnerabilityConcern1 = ComponentConcern.vulnerability(ItemOperation.ADD, "CVE-123", ComponentConcernSeverity.CRITICAL, "https://vuln-rul");
        ComponentConcern vulnerabilityConcern2 = ComponentConcern.vulnerability(ItemOperation.ADD, "CVE-456", ComponentConcernSeverity.CRITICAL, "https://vuln-rul");
        ComponentConcern vulnerabilityConcern3 = ComponentConcern.vulnerability(ItemOperation.ADD, "CVE-789", ComponentConcernSeverity.CRITICAL, "https://vuln-rul");
        ComponentConcern vulnerabilityConcern4 = ComponentConcern.vulnerability(ItemOperation.DELETE, "CVE-246", ComponentConcernSeverity.MINOR_MEDIUM, "https://vuln-rul");

        BomComponentDetails bomComponentDetails = createBomComponentDetails(List.of(vulnerabilityConcern1, vulnerabilityConcern2, vulnerabilityConcern3, vulnerabilityConcern4));

        ProjectMessage projectMessage = ProjectMessage.componentConcern(providerDetails, commonProject, commonProjectVersion, List.of(bomComponentDetails));
        ProcessedProviderMessage<ProjectMessage> processedProviderMessage = new ProcessedProviderMessage<>(Set.of(1L), projectMessage);

        ProcessedProviderMessage<SimpleMessage> summarizedSimpleMessage = projectMessageSummarizer.summarize(processedProviderMessage);
        SimpleMessage simpleMessage = summarizedSimpleMessage.getProviderMessage();
        printSimpleMessage(simpleMessage);

        testProjectStatus(simpleMessage);
        testComponentStatus(simpleMessage);

        assertEquals(4, simpleMessage.getDetails().size());
        assertEquals(3, Integer.valueOf(getDetailValue(simpleMessage.getDetails(), vulnerabilityConcern1.getSeverity().getVulnerabilityLabel(), "Vulnerabilities", ProjectMessageSummarizer.OP_PARTICIPLE_ADDED)));
        assertEquals(1, Integer.valueOf(getDetailValue(simpleMessage.getDetails(), vulnerabilityConcern4.getSeverity().getVulnerabilityLabel(), "Vulnerabilities", ProjectMessageSummarizer.OP_PARTICIPLE_DELETED)));
    }

    private void testProjectStatus(SimpleMessage simpleMessage) {
        String summary = simpleMessage.getSummary();
        assertTrue(summary.contains(provider.getLabel()));
        assertTrue(summary.contains(commonProject.getValue()));

        String description = simpleMessage.getDescription();
        assertTrue(description.contains(commonProject.getValue()));

        assertEquals(providerDetails, simpleMessage.getProviderDetails());
        assertEquals(provider, simpleMessage.getProvider());
    }

    private void testComponentStatus(SimpleMessage simpleMessage) {
        assertTrue(simpleMessage.getDetails().size() >= 2);
        assertTrue(simpleMessage.getDetails().contains(commonProject));
        assertTrue(simpleMessage.getDetails().contains(commonProjectVersion));

        assertTrue(simpleMessage.getSummary().contains(commonProjectVersion.getValue()));
        assertTrue(simpleMessage.getDescription().contains(commonProjectVersion.getValue()));

        String description = simpleMessage.getDescription();
        assertTrue(description.contains("component"));
    }

    private boolean doesLabelExist(List<LinkableItem> details, String severity, String componentConcernTypeName, String participle) {
        return details
                   .stream()
                   .anyMatch(linkableItem -> doesLabelExist(linkableItem, severity, componentConcernTypeName, participle));
    }

    private boolean doesLabelExist(LinkableItem linkableItem, String severity, String componentConcernTypeName, String participle) {
        String label = linkableItem.getLabel();
        return label.contains(severity) && label.contains(componentConcernTypeName) && label.contains(participle);
    }

    private String getDetailValue(List<LinkableItem> details, String severity, String componentConcernTypeName, String participle) {
        Predicate<LinkableItem> filter = (linkableItem) -> doesLabelExist(linkableItem, severity, componentConcernTypeName, participle);
        return details
                   .stream()
                   .filter(filter)
                   .findFirst()
                   .map(LinkableItem::getValue)
                   .orElseThrow(() -> new RuntimeException("Could not find the linkable Item"));
    }

    private BomComponentDetails createBomComponentDetails(List<ComponentConcern> componentConcerns) {
        ComponentPolicy componentPolicy = new ComponentPolicy("A component policy", ComponentConcernSeverity.UNSPECIFIED_UNKNOWN, true, false, null, "Uncategorized");

        return new BomComponentDetails(
            component,
            componentVersion,
            ComponentVulnerabilities.none(),
            List.of(componentPolicy),
            componentConcerns,
            new LinkableItem("License", "The software license name", "https://license-url"),
            "The usage of the component",
            ComponentUpgradeGuidance.none(),
            List.of(),
            "https://blackduck-issues-url"
        );
    }

    private void printSimpleMessage(SimpleMessage simpleMessage) {
        System.out.println("Summary: " + simpleMessage.getSummary());
        System.out.println("Description: " + simpleMessage.getDescription());
        System.out.println("Details: ");
        for (LinkableItem linkableItem : simpleMessage.getDetails()) {
            System.out.println("Label: " + linkableItem.getLabel() + " | Value: " + linkableItem.getValue());
        }
    }
}
