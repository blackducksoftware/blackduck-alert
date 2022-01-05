package com.synopsys.integration.alert.processor.api.extract.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectOperation;

public class ProcessedProviderMessageHolderTest {
    private final LinkableItem commonProject = new LinkableItem("Project", "Common Project");
    private final ProjectOperation commonOperation = ProjectOperation.CREATE;

    private final LinkableItem provider1 = new LinkableItem("Provider", "Provider 1");
    private final ProviderDetails providerDetails1 = new ProviderDetails(1L, provider1);
    private final ProjectMessage projectMessage1 = ProjectMessage.projectStatusInfo(providerDetails1, commonProject, commonOperation);

    private final LinkableItem provider2 = new LinkableItem("Provider", "Provider 2");
    private final ProviderDetails providerDetails2 = new ProviderDetails(2L, provider2);
    private final ProjectMessage projectMessage2 = ProjectMessage.projectStatusInfo(providerDetails2, commonProject, commonOperation);

    private final SimpleMessage simpleMessage1 = SimpleMessage.original(providerDetails1, "summary", "description", List.of());
    private final SimpleMessage simpleMessage2 = SimpleMessage.original(providerDetails2, "summary2", "description2", List.of());

    private final ProcessedProviderMessage<ProjectMessage> processedProviderMessage1 = ProcessedProviderMessage.singleSource(10L, projectMessage1);
    private final ProcessedProviderMessage<ProjectMessage> processedProviderMessage2 = ProcessedProviderMessage.singleSource(20L, projectMessage2);
    private final ProcessedProviderMessage<SimpleMessage> processedSimpleMessage1 = ProcessedProviderMessage.singleSource(11L, simpleMessage1);
    private final ProcessedProviderMessage<SimpleMessage> processedSimpleMessage2 = ProcessedProviderMessage.singleSource(22L, simpleMessage2);

    @Test
    public void reducePopulatedTest() {
        ProcessedProviderMessageHolder lhs = new ProcessedProviderMessageHolder(List.of(processedProviderMessage1), List.of(processedSimpleMessage1));
        ProcessedProviderMessageHolder rhs = new ProcessedProviderMessageHolder(List.of(processedProviderMessage2), List.of(processedSimpleMessage2));
        reduceAndAssert(lhs, rhs);
    }

    @Test
    public void reduceEmptyTest() {
        ProcessedProviderMessageHolder lhs = ProcessedProviderMessageHolder.empty();
        ProcessedProviderMessageHolder rhs = ProcessedProviderMessageHolder.empty();
        reduceAndAssert(lhs, rhs);
    }

    @Test
    public void reduceEmptyLeftTest() {
        ProcessedProviderMessageHolder lhs = ProcessedProviderMessageHolder.empty();
        ProcessedProviderMessageHolder rhs = new ProcessedProviderMessageHolder(List.of(processedProviderMessage1), List.of(processedSimpleMessage1));
        reduceAndAssert(lhs, rhs);
    }

    @Test
    public void reduceEmptyRightTest() {
        ProcessedProviderMessageHolder lhs = new ProcessedProviderMessageHolder(List.of(processedProviderMessage1), List.of(processedSimpleMessage1));
        ProcessedProviderMessageHolder rhs = ProcessedProviderMessageHolder.empty();
        reduceAndAssert(lhs, rhs);
    }

    @Test
    public void expandTest() {
        ProcessedProviderMessageHolder processedProviderMessageHolder = new ProcessedProviderMessageHolder(List.of(processedProviderMessage1, processedProviderMessage2), List.of(processedSimpleMessage1, processedSimpleMessage2));
        List<ProcessedProviderMessageHolder> messageHolders = processedProviderMessageHolder.expand();

        assertEquals(4, messageHolders.size());
    }

    @Test
    public void expandEmptyTest() {
        ProcessedProviderMessageHolder processedProviderMessageHolder = ProcessedProviderMessageHolder.empty();
        List<ProcessedProviderMessageHolder> messageHolders = processedProviderMessageHolder.expand();

        assertTrue(messageHolders.isEmpty());
    }

    @Test
    public void toProviderMessageHolderTest() {
        ProcessedProviderMessageHolder processedProviderMessageHolder = new ProcessedProviderMessageHolder(List.of(processedProviderMessage1, processedProviderMessage2), List.of(processedSimpleMessage1, processedSimpleMessage2));
        ProviderMessageHolder providerMessageHolder = processedProviderMessageHolder.toProviderMessageHolder();

        assertEquals(2, providerMessageHolder.getProjectMessages().size());
        assertEquals(2, providerMessageHolder.getSimpleMessages().size());
    }

    @Test
    public void toProviderMessageHolderEmptyTest() {
        ProcessedProviderMessageHolder processedProviderMessageHolder = ProcessedProviderMessageHolder.empty();
        ProviderMessageHolder providerMessageHolder = processedProviderMessageHolder.toProviderMessageHolder();

        assertTrue(providerMessageHolder.getProjectMessages().isEmpty());
        assertTrue(providerMessageHolder.getSimpleMessages().isEmpty());
    }

    @Test
    public void extractAllNotificationIds() {
        ProcessedProviderMessage<ProjectMessage> processedProviderMessageDuplicateId = ProcessedProviderMessage.singleSource(10L, projectMessage1);

        ProcessedProviderMessageHolder processedProviderMessageHolder = new ProcessedProviderMessageHolder(List.of(processedProviderMessage1, processedProviderMessage2, processedProviderMessageDuplicateId),
            List.of(processedSimpleMessage1, processedSimpleMessage2));
        Set<Long> notificationIds = processedProviderMessageHolder.extractAllNotificationIds();

        assertEquals(4, notificationIds.size());
    }

    @Test
    public void getProcessedProjectMessagesTest() {
        ProcessedProviderMessageHolder processedProviderMessageHolder = new ProcessedProviderMessageHolder(List.of(processedProviderMessage1, processedProviderMessage2), List.of(processedSimpleMessage1, processedSimpleMessage2));
        List<ProcessedProviderMessage<ProjectMessage>> processedProjectMessages = processedProviderMessageHolder.getProcessedProjectMessages();

        assertEquals(2, processedProjectMessages.size());
    }

    @Test
    public void getProcessedSimpleMessagesTest() {
        ProcessedProviderMessageHolder processedProviderMessageHolder = new ProcessedProviderMessageHolder(List.of(processedProviderMessage1, processedProviderMessage2), List.of(processedSimpleMessage1, processedSimpleMessage2));
        List<ProcessedProviderMessage<SimpleMessage>> processedSimpleMessages = processedProviderMessageHolder.getProcessedSimpleMessages();

        assertEquals(2, processedSimpleMessages.size());
    }

    private void reduceAndAssert(ProcessedProviderMessageHolder left, ProcessedProviderMessageHolder right) {
        ProcessedProviderMessageHolder reduced = ProcessedProviderMessageHolder.reduce(left, right);
        assertGetter(left, right, reduced, ProcessedProviderMessageHolder::getProcessedProjectMessages);
        assertGetter(left, right, reduced, ProcessedProviderMessageHolder::getProcessedSimpleMessages);
    }

    private <T extends ProviderMessage<T>> void assertGetter(
        ProcessedProviderMessageHolder left,
        ProcessedProviderMessageHolder right,
        ProcessedProviderMessageHolder reduced,
        Function<ProcessedProviderMessageHolder, List<ProcessedProviderMessage<T>>> getter
    ) {
        int projectMessageSize = getter.apply(left).size() + getter.apply(right).size();
        assertEquals(projectMessageSize, getter.apply(reduced).size());
    }
}
