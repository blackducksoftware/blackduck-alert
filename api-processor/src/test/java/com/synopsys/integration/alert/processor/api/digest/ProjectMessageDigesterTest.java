package com.synopsys.integration.alert.processor.api.digest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.ProcessedProviderMessage;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectOperation;

public class ProjectMessageDigesterTest {
    private static final LinkableItem PROVIDER = new LinkableItem("provider", "p1", "https://provider");
    private static final ProviderDetails PROVIDER_DETAILS = new ProviderDetails(0L, PROVIDER);

    private final ProjectMessageDigester projectMessageDigester = new ProjectMessageDigester();

    @Test
    public void digestCollapseTest() {
        String projectName = "p1";
        ProcessedProviderMessage<ProjectMessage> processedProviderMessage1 = createProcessedProviderMessage(projectName, ProjectOperation.CREATE);
        ProcessedProviderMessage<ProjectMessage> processedProviderMessage2 = createProcessedProviderMessage(projectName, ProjectOperation.DELETE);

        List<ProcessedProviderMessage<ProjectMessage>> digestedMessages = projectMessageDigester.digest(List.of(processedProviderMessage1, processedProviderMessage2));
        assertEquals(0, digestedMessages.size());
    }

    @Test
    public void digestCombineTest() {
        String projectName = "p1";
        ProcessedProviderMessage<ProjectMessage> processedProviderMessage1 = createProcessedProviderMessage(projectName, ProjectOperation.CREATE);
        ProcessedProviderMessage<ProjectMessage> processedProviderMessage2 = createProcessedProviderMessage(projectName, ProjectOperation.CREATE);

        List<ProcessedProviderMessage<ProjectMessage>> digestedMessages = projectMessageDigester.digest(List.of(processedProviderMessage1, processedProviderMessage2));
        assertEquals(1, digestedMessages.size());
    }

    @Test
    public void digestKeepTest() {
        ProcessedProviderMessage<ProjectMessage> processedProviderMessage1 = createProcessedProviderMessage("proj1", ProjectOperation.CREATE);
        ProcessedProviderMessage<ProjectMessage> processedProviderMessage2 = createProcessedProviderMessage("other proj", ProjectOperation.DELETE);

        List<ProcessedProviderMessage<ProjectMessage>> digestedMessages = projectMessageDigester.digest(List.of(processedProviderMessage1, processedProviderMessage2));
        assertEquals(2, digestedMessages.size());
    }

    private ProcessedProviderMessage<ProjectMessage> createProcessedProviderMessage(String projectName, ProjectOperation operation) {
        LinkableItem project1 = new LinkableItem("proj", projectName, "https://" + projectName);
        ProjectMessage projectMessage1 = ProjectMessage.projectStatusInfo(PROVIDER_DETAILS, project1, operation);
        return new ProcessedProviderMessage<>(Set.of(), projectMessage1);
    }

}
