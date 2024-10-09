package com.blackduck.integration.alert.api.channel.issue.tracker.search;

import java.io.Serializable;

import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.common.message.model.LinkableItem;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderDetails;

public interface ProjectVersionIssueFinder<T extends Serializable> {
    IssueTrackerSearchResult<T> findProjectVersionIssues(ProviderDetails providerDetails, LinkableItem project, LinkableItem projectVersion) throws AlertException;

}
