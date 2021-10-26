package com.synopsys.integration.alert.processor.api.extract.model.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;

public class ProjectMessageTest {
    @Test
    public void combineProviderDetailsDontMatchTest() {
        LinkableItem commonProject = new LinkableItem("Project", "Common Project");
        ProjectOperation commonOperation = ProjectOperation.CREATE;

        LinkableItem provider1 = new LinkableItem("Provider", "Provider 1");
        ProviderDetails providerDetails1 = new ProviderDetails(1L, provider1);
        ProjectMessage projectMessage1 = ProjectMessage.projectStatusInfo(providerDetails1, commonProject, commonOperation);

        LinkableItem provider2 = new LinkableItem("Provider", "Provider 2");
        ProviderDetails providerDetails2 = new ProviderDetails(2L, provider2);
        ProjectMessage projectMessage2 = ProjectMessage.projectStatusInfo(providerDetails2, commonProject, commonOperation);

        assertNotCombined(projectMessage1, projectMessage2);
    }

    @Test
    public void combineMessageReasonsDontMatchTest() {
        LinkableItem provider = new LinkableItem("Provider", "Provider Config Name");
        ProviderDetails providerDetails = new ProviderDetails(1L, provider);
        LinkableItem commonProject = new LinkableItem("Project", "Common Project");
        ProjectOperation commonOperation = ProjectOperation.CREATE;

        ProjectMessage projectMessage1 = ProjectMessage.projectStatusInfo(providerDetails, commonProject, commonOperation);

        LinkableItem projectVersion = new LinkableItem("Project Version", "1.2.3");
        ProjectMessage projectMessage2 = ProjectMessage.projectVersionStatusInfo(providerDetails, commonProject, projectVersion, commonOperation);

        assertNotCombined(projectMessage1, projectMessage2);
    }

    @Test
    public void combineProjectsDontMatchTest() {
        LinkableItem provider = new LinkableItem("Provider", "Provider Config Name");
        ProviderDetails providerDetails = new ProviderDetails(1L, provider);
        LinkableItem project1 = new LinkableItem("Project", "Project 1");
        LinkableItem project2 = new LinkableItem("Project", "Project 2");
        ProjectOperation commonOperation = ProjectOperation.CREATE;

        ProjectMessage projectMessage1 = ProjectMessage.projectStatusInfo(providerDetails, project1, commonOperation);
        ProjectMessage projectMessage2 = ProjectMessage.projectStatusInfo(providerDetails, project2, commonOperation);

        assertNotCombined(projectMessage1, projectMessage2);
    }

    @Test
    public void combineProjectOperationsDontMatchTest() {
        LinkableItem provider = new LinkableItem("Provider", "Provider Config Name");
        ProviderDetails providerDetails = new ProviderDetails(1L, provider);
        LinkableItem commonProject = new LinkableItem("Project", "Project 1");

        ProjectMessage projectMessage1 = ProjectMessage.projectStatusInfo(providerDetails, commonProject, ProjectOperation.CREATE);
        ProjectMessage projectMessage2 = ProjectMessage.projectStatusInfo(providerDetails, commonProject, ProjectOperation.DELETE);

        // Create then delete
        List<ProjectMessage> combinedProjectMessages1 = projectMessage1.combine(projectMessage2);
        assertEquals(0, combinedProjectMessages1.size());

        // Delete then create
        List<ProjectMessage> combinedProjectMessages2 = projectMessage2.combine(projectMessage1);
        assertEquals(1, combinedProjectMessages2.size());
        assertEquals(projectMessage1, combinedProjectMessages2.get(0));
    }

    @Test
    public void combineProjectOperationsMatchTest() {
        LinkableItem provider = new LinkableItem("Provider", "Provider Config Name");
        ProviderDetails providerDetails = new ProviderDetails(1L, provider);
        LinkableItem commonProject = new LinkableItem("Project", "Project 1");
        ProjectOperation commonOperation = ProjectOperation.CREATE;

        ProjectMessage projectMessage1 = ProjectMessage.projectStatusInfo(providerDetails, commonProject, commonOperation);
        ProjectMessage projectMessage2 = ProjectMessage.projectStatusInfo(providerDetails, commonProject, commonOperation);

        assertCombined(projectMessage1, projectMessage2);
    }

    @Test
    public void combineProjectVersionsDontMatchTest() {
        LinkableItem provider = new LinkableItem("Provider", "Provider Config Name");
        ProviderDetails providerDetails = new ProviderDetails(1L, provider);
        LinkableItem commonProject = new LinkableItem("Project", "Project 1");
        ProjectOperation commonOperation = ProjectOperation.CREATE;

        LinkableItem projectVersion1 = new LinkableItem("Project Version", "Project Version 1");
        LinkableItem projectVersion2 = new LinkableItem("Project Version", "Project Version 2");

        ProjectMessage projectMessage1 = ProjectMessage.projectVersionStatusInfo(providerDetails, commonProject, null, commonOperation);
        ProjectMessage projectMessage2 = ProjectMessage.projectVersionStatusInfo(providerDetails, commonProject, projectVersion1, commonOperation);
        ProjectMessage projectMessage3 = ProjectMessage.projectVersionStatusInfo(providerDetails, commonProject, projectVersion2, commonOperation);

        assertNotCombined(projectMessage1, projectMessage2);
        assertNotCombined(projectMessage1, projectMessage3);
        assertNotCombined(projectMessage2, projectMessage3);
    }

    @Test
    public void combineProjectVersionOperationsDontMatchTest() {
        LinkableItem provider = new LinkableItem("Provider", "Provider Config Name");
        ProviderDetails providerDetails = new ProviderDetails(1L, provider);
        LinkableItem commonProject = new LinkableItem("Project", "Project 1");
        LinkableItem commonProjectVersion = new LinkableItem("Project Version", "Project Version 1");

        ProjectMessage projectMessage1 = ProjectMessage.projectVersionStatusInfo(providerDetails, commonProject, commonProjectVersion, ProjectOperation.CREATE);
        ProjectMessage projectMessage2 = ProjectMessage.projectVersionStatusInfo(providerDetails, commonProject, commonProjectVersion, ProjectOperation.DELETE);

        // Create then delete
        List<ProjectMessage> combinedProjectMessages1 = projectMessage1.combine(projectMessage2);
        assertEquals(0, combinedProjectMessages1.size());

        // Delete then create
        List<ProjectMessage> combinedProjectMessages2 = projectMessage2.combine(projectMessage1);
        assertEquals(1, combinedProjectMessages2.size());
        assertEquals(projectMessage1, combinedProjectMessages2.get(0));
    }

    @Test
    public void combineProjectVersionOperationsMatchTest() {
        LinkableItem provider = new LinkableItem("Provider", "Provider Config Name");
        ProviderDetails providerDetails = new ProviderDetails(1L, provider);
        LinkableItem commonProject = new LinkableItem("Project", "Project 1");
        LinkableItem commonProjectVersion = new LinkableItem("Project Version", "Project Version 1");
        ProjectOperation commonOperation = ProjectOperation.CREATE;

        ProjectMessage projectMessage1 = ProjectMessage.projectVersionStatusInfo(providerDetails, commonProject, commonProjectVersion, commonOperation);
        ProjectMessage projectMessage2 = ProjectMessage.projectVersionStatusInfo(providerDetails, commonProject, commonProjectVersion, commonOperation);

        assertCombined(projectMessage1, projectMessage2);
    }

    @Test
    public void hasBomComponentsTest() {
        LinkableItem provider = new LinkableItem("Provider", "Provider Config Name");
        ProviderDetails providerDetails = new ProviderDetails(1L, provider);
        LinkableItem commonProject = new LinkableItem("Project", "Project 1");
        LinkableItem commonProjectVersion = new LinkableItem("Project Version", "Project Version 1");
        BomComponentDetails bomComponentDetails = new BomComponentDetails(
            new LinkableItem("Component", "The component"),
            commonProjectVersion,
            ComponentVulnerabilities.none(),
            List.of(),
            List.of(),
            new LinkableItem("License", "The software license name", "https://license-url"),
            "The usage of the component",
            ComponentUpgradeGuidance.none(),
            List.of(),
            "https://blackduck-issues-url"
        );
        ProjectMessage projectMessage = ProjectMessage.componentConcern(providerDetails, commonProject, commonProjectVersion, List.of(bomComponentDetails));
        assertTrue(projectMessage.hasBomComponents());
        assertEquals(1, projectMessage.getBomComponents().size());
        assertEquals(bomComponentDetails, projectMessage.getBomComponents().get(0));
    }

    // Assertions

    private void assertNotCombined(ProjectMessage projectMessage1, ProjectMessage projectMessage2) {
        List<ProjectMessage> combinedProjectMessages1 = projectMessage1.combine(projectMessage2);
        assertEquals(2, combinedProjectMessages1.size());

        List<ProjectMessage> combinedProjectMessages2 = projectMessage2.combine(projectMessage1);
        assertEquals(2, combinedProjectMessages2.size());
    }

    private void assertCombined(ProjectMessage projectMessage1, ProjectMessage projectMessage2) {
        List<ProjectMessage> combinedProjectMessages1 = projectMessage1.combine(projectMessage2);
        assertEquals(1, combinedProjectMessages1.size());
        assertEquals(projectMessage1, combinedProjectMessages1.get(0));

        List<ProjectMessage> combinedProjectMessages2 = projectMessage2.combine(projectMessage1);
        assertEquals(1, combinedProjectMessages2.size());
        assertEquals(projectMessage2, combinedProjectMessages2.get(0));
    }

}
