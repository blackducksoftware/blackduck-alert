package com.synopsys.integration.alert.channel.azure.boards.distribution.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.channel.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

class AzureBoardsCreateIssueEventTest {
    @Test
    void testObjectConstruction() {
        String destination = "destination_queue";
        UUID jobId = UUID.randomUUID();
        IssueCreationModel model = IssueCreationModel.simple("title", "description", List.of(), new LinkableItem("providerLabel", "provider"));
        AzureBoardsCreateIssueEvent event = new AzureBoardsCreateIssueEvent(destination, jobId, model);

        assertNotNull(event.getEventId());
        assertEquals(destination, event.getDestination());
        assertEquals(jobId, event.getJobId());
        assertEquals(model, event.getCreationModel());
    }
}
