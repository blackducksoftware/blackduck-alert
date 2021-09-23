package com.synopsys.integration.alert.processor.api.extract.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectOperation;

public class SimpleMessageTest {
    private static final String SUMMARY = "Message Summary";
    private static final String DESCRIPTION = "A detailed description";

    private final LinkableItem provider = new LinkableItem("Provider", "BlackDuck provider");
    private final ProviderDetails providerDetails = new ProviderDetails(1L, provider);
    private final LinkableItem details = new LinkableItem("A message detail", "1");
    private final ProjectMessage source = ProjectMessage.projectStatusInfo(providerDetails, null, ProjectOperation.CREATE);

    @Test
    public void getSummaryTest() {
        SimpleMessage simpleMessage = SimpleMessage.original(providerDetails, SUMMARY, DESCRIPTION, List.of(details));
        assertEquals(SUMMARY, simpleMessage.getSummary());
    }

    @Test
    public void getDescriptionTest() {
        SimpleMessage simpleMessage = SimpleMessage.original(providerDetails, SUMMARY, DESCRIPTION, List.of(details));
        assertEquals(DESCRIPTION, simpleMessage.getDescription());
    }

    @Test
    public void getDetailsTest() {
        SimpleMessage simpleMessage = SimpleMessage.original(providerDetails, SUMMARY, DESCRIPTION, List.of(details));
        assertEquals(1, simpleMessage.getDetails().size());
        assertEquals(details, simpleMessage.getDetails().get(0));
    }

    @Test
    public void getSourceTest() {
        SimpleMessage simpleMessage = SimpleMessage.original(providerDetails, SUMMARY, DESCRIPTION, List.of(details));
        SimpleMessage simpleMessageDerived = SimpleMessage.derived(SUMMARY, DESCRIPTION, List.of(details), source);

        assertTrue(simpleMessage.getSource().isEmpty());

        assertTrue(simpleMessageDerived.getSource().isPresent());
        assertEquals(source, simpleMessageDerived.getSource().get());
    }

    @Test
    public void combineTest() {
        SimpleMessage simpleMessage = SimpleMessage.original(providerDetails, SUMMARY, DESCRIPTION, List.of(details));
        SimpleMessage simpleMessageDerived = SimpleMessage.derived(SUMMARY, DESCRIPTION, List.of(details), source);

        List<SimpleMessage> combinedMessages = simpleMessage.combine(simpleMessageDerived);

        assertEquals(2, combinedMessages.size());
        assertTrue(combinedMessages.contains(simpleMessage));
        assertTrue(combinedMessages.contains(simpleMessageDerived));
    }
}
