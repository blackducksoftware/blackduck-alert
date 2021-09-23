package com.synopsys.integration.alert.processor.api.extract.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectOperation;

public class ProcessedProviderMessageTest {
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

        ProcessedProviderMessage<ProjectMessage> processedProviderMessage1 = ProcessedProviderMessage.singleSource(10L, projectMessage1);
        ProcessedProviderMessage<ProjectMessage> processedProviderMessage2 = ProcessedProviderMessage.singleSource(20L, projectMessage2);
        assertNotCombined(processedProviderMessage1, processedProviderMessage2);
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

        ProcessedProviderMessage<ProjectMessage> processedProviderMessage1 = ProcessedProviderMessage.singleSource(10L, projectMessage1);
        ProcessedProviderMessage<ProjectMessage> processedProviderMessage2 = ProcessedProviderMessage.singleSource(20L, projectMessage2);
        assertNotCombined(processedProviderMessage1, processedProviderMessage2);
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

        ProcessedProviderMessage<ProjectMessage> processedProviderMessage1 = ProcessedProviderMessage.singleSource(10L, projectMessage1);
        ProcessedProviderMessage<ProjectMessage> processedProviderMessage2 = ProcessedProviderMessage.singleSource(20L, projectMessage2);
        assertNotCombined(processedProviderMessage1, processedProviderMessage2);
    }

    @Test
    public void combineProjectOperationsDontMatchTest() {
        LinkableItem provider = new LinkableItem("Provider", "Provider Config Name");
        ProviderDetails providerDetails = new ProviderDetails(1L, provider);
        LinkableItem commonProject = new LinkableItem("Project", "Project 1");

        ProjectMessage projectMessage1 = ProjectMessage.projectStatusInfo(providerDetails, commonProject, ProjectOperation.CREATE);
        ProjectMessage projectMessage2 = ProjectMessage.projectStatusInfo(providerDetails, commonProject, ProjectOperation.DELETE);

        ProcessedProviderMessage<ProjectMessage> processedProviderMessage1 = ProcessedProviderMessage.singleSource(10L, projectMessage1);
        ProcessedProviderMessage<ProjectMessage> processedProviderMessage2 = ProcessedProviderMessage.singleSource(20L, projectMessage2);

        // Create then delete
        List<ProcessedProviderMessage<ProjectMessage>> combinedProjectMessages1 = processedProviderMessage1.combine(processedProviderMessage2);
        assertEquals(0, combinedProjectMessages1.size());

        // Delete then create
        List<ProcessedProviderMessage<ProjectMessage>> combinedProjectMessages2 = processedProviderMessage2.combine(processedProviderMessage1);
        assertEquals(1, combinedProjectMessages2.size());
        assertEquals(processedProviderMessage1.getProviderMessage(), combinedProjectMessages2.get(0).getProviderMessage());
    }

    @Test
    public void combineProjectOperationsMatchTest() {
        LinkableItem provider = new LinkableItem("Provider", "Provider Config Name");
        ProviderDetails providerDetails = new ProviderDetails(1L, provider);
        LinkableItem commonProject = new LinkableItem("Project", "Project 1");
        ProjectOperation commonOperation = ProjectOperation.CREATE;

        ProjectMessage projectMessage1 = ProjectMessage.projectStatusInfo(providerDetails, commonProject, commonOperation);
        ProjectMessage projectMessage2 = ProjectMessage.projectStatusInfo(providerDetails, commonProject, commonOperation);

        ProcessedProviderMessage<ProjectMessage> processedProviderMessage1 = ProcessedProviderMessage.singleSource(10L, projectMessage1);
        ProcessedProviderMessage<ProjectMessage> processedProviderMessage2 = ProcessedProviderMessage.singleSource(20L, projectMessage2);
        assertCombined(processedProviderMessage1, processedProviderMessage2);
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

        ProcessedProviderMessage<ProjectMessage> processedProviderMessage1 = ProcessedProviderMessage.singleSource(10L, projectMessage1);
        ProcessedProviderMessage<ProjectMessage> processedProviderMessage2 = ProcessedProviderMessage.singleSource(20L, projectMessage2);
        ProcessedProviderMessage<ProjectMessage> processedProviderMessage3 = ProcessedProviderMessage.singleSource(30L, projectMessage3);

        assertNotCombined(processedProviderMessage1, processedProviderMessage2);
        assertNotCombined(processedProviderMessage1, processedProviderMessage3);
        assertNotCombined(processedProviderMessage2, processedProviderMessage3);
    }

    @Test
    public void combineProjectVersionOperationsDontMatchTest() {
        LinkableItem provider = new LinkableItem("Provider", "Provider Config Name");
        ProviderDetails providerDetails = new ProviderDetails(1L, provider);
        LinkableItem commonProject = new LinkableItem("Project", "Project 1");
        LinkableItem commonProjectVersion = new LinkableItem("Project Version", "Project Version 1");

        ProjectMessage projectMessage1 = ProjectMessage.projectVersionStatusInfo(providerDetails, commonProject, commonProjectVersion, ProjectOperation.CREATE);
        ProjectMessage projectMessage2 = ProjectMessage.projectVersionStatusInfo(providerDetails, commonProject, commonProjectVersion, ProjectOperation.DELETE);

        ProcessedProviderMessage<ProjectMessage> processedProviderMessage1 = ProcessedProviderMessage.singleSource(10L, projectMessage1);
        ProcessedProviderMessage<ProjectMessage> processedProviderMessage2 = ProcessedProviderMessage.singleSource(20L, projectMessage2);

        // Create then delete
        List<ProcessedProviderMessage<ProjectMessage>> combinedProjectMessages1 = processedProviderMessage1.combine(processedProviderMessage2);
        assertEquals(0, combinedProjectMessages1.size());

        // Delete then create
        List<ProcessedProviderMessage<ProjectMessage>> combinedProjectMessages2 = processedProviderMessage2.combine(processedProviderMessage1);
        assertEquals(1, combinedProjectMessages2.size());
        assertEquals(processedProviderMessage1.getProviderMessage(), combinedProjectMessages2.get(0).getProviderMessage());
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

        ProcessedProviderMessage<ProjectMessage> processedProviderMessage1 = ProcessedProviderMessage.singleSource(10L, projectMessage1);
        ProcessedProviderMessage<ProjectMessage> processedProviderMessage2 = ProcessedProviderMessage.singleSource(20L, projectMessage2);

        assertCombined(processedProviderMessage1, processedProviderMessage2);
    }

    // Assertions

    private void assertNotCombined(ProcessedProviderMessage<ProjectMessage> projectMessage1, ProcessedProviderMessage<ProjectMessage> projectMessage2) {
        List<ProcessedProviderMessage<ProjectMessage>> combinedProjectMessages1 = projectMessage1.combine(projectMessage2);
        assertEquals(2, combinedProjectMessages1.size());
        assertEquals(projectMessage1.getNotificationIds(), combinedProjectMessages1.get(0).getNotificationIds());
        assertEquals(projectMessage2.getNotificationIds(), combinedProjectMessages1.get(1).getNotificationIds());

        List<ProcessedProviderMessage<ProjectMessage>> combinedProjectMessages2 = projectMessage2.combine(projectMessage1);
        assertEquals(2, combinedProjectMessages2.size());
        assertEquals(projectMessage2.getNotificationIds(), combinedProjectMessages2.get(0).getNotificationIds());
        assertEquals(projectMessage1.getNotificationIds(), combinedProjectMessages2.get(1).getNotificationIds());
    }

    private void assertCombined(ProcessedProviderMessage<ProjectMessage> projectMessage1, ProcessedProviderMessage<ProjectMessage> projectMessage2) {
        List<ProcessedProviderMessage<ProjectMessage>> combinedProjectMessages1 = projectMessage1.combine(projectMessage2);
        assertEquals(1, combinedProjectMessages1.size());
        assertEquals(projectMessage1.getProviderMessage(), combinedProjectMessages1.get(0).getProviderMessage());

        Set<Long> combinedNotificationIds1 = combinedProjectMessages1.get(0).getNotificationIds();
        assertTrue(combinedNotificationIds1.containsAll(projectMessage1.getNotificationIds()));
        assertTrue(combinedNotificationIds1.containsAll(projectMessage2.getNotificationIds()));

        List<ProcessedProviderMessage<ProjectMessage>> combinedProjectMessages2 = projectMessage2.combine(projectMessage1);
        assertEquals(1, combinedProjectMessages2.size());
        assertEquals(projectMessage2.getProviderMessage(), combinedProjectMessages2.get(0).getProviderMessage());

        Set<Long> combinedNotificationIds2 = combinedProjectMessages2.get(0).getNotificationIds();
        assertTrue(combinedNotificationIds2.containsAll(projectMessage1.getNotificationIds()));
        assertTrue(combinedNotificationIds2.containsAll(projectMessage2.getNotificationIds()));
    }

}
