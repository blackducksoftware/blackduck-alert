package com.synopsys.integration.alert.api.channel.issue.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.channel.issue.model.IssueCommentModel;

class IssueTrackerCommentEventTest {
    @Test
    void testObjectConstruction() {
        String destination = "destination_queue";
        UUID jobId = UUID.randomUUID();
        IssueCommentModel<String> model = new IssueCommentModel<>(null, List.of(), null);
        IssueTrackerCommentEvent<String> event = new IssueTrackerCommentEvent<>(destination, jobId, model);

        assertNotNull(event.getEventId());
        assertEquals(destination, event.getDestination());
        assertEquals(jobId, event.getJobId());
        assertEquals(model, event.getCommentModel());
    }
}
