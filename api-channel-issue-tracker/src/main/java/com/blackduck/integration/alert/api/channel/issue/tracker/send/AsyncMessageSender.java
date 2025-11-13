package com.blackduck.integration.alert.api.channel.issue.tracker.send;

import java.util.List;

public interface AsyncMessageSender<T> {
    void sendAsyncMessages(List<T> issueTrackerMessages);
}
