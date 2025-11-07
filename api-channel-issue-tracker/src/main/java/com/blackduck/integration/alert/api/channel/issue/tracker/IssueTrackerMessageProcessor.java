package com.blackduck.integration.alert.api.channel.issue.tracker;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerResponse;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderMessageHolder;

import java.io.Serializable;

public interface IssueTrackerMessageProcessor<T extends Serializable> {

    IssueTrackerResponse<T> processMessages(ProviderMessageHolder messages, String jobName) throws AlertException;
}
