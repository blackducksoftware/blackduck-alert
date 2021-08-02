/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.search;

import java.io.Serializable;
import java.util.List;

import com.synopsys.integration.alert.api.channel.issue.convert.ProjectMessageToIssueModelTransformer;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;

public abstract class IssueTrackerSearcher<T extends Serializable> {
    private final IssueTrackerSearcherV2<T> issueTrackerSearcherV2;

    protected IssueTrackerSearcher(ProjectMessageToIssueModelTransformer modelTransformer) {
        this.issueTrackerSearcherV2 = new IssueTrackerSearcherV2<>(
            this::findProjectIssues,
            this::findProjectVersionIssues,
            this::findIssuesByComponent,
            this::findExistingIssuesByProjectIssueModel,
            modelTransformer
        );
    }

    public final List<ActionableIssueSearchResult<T>> findIssues(ProjectMessage projectMessage) throws AlertException {
        return issueTrackerSearcherV2.findIssues(projectMessage);
    }

    protected abstract List<ProjectIssueSearchResult<T>> findProjectIssues(ProviderDetails providerDetails, LinkableItem project) throws AlertException;

    protected abstract List<ProjectIssueSearchResult<T>> findProjectVersionIssues(ProviderDetails providerDetails, LinkableItem project, LinkableItem projectVersion) throws AlertException;

    protected abstract List<ProjectIssueSearchResult<T>> findIssuesByComponent(ProviderDetails providerDetails, LinkableItem project, LinkableItem projectVersion, BomComponentDetails bomComponent) throws AlertException;

    protected abstract List<ExistingIssueDetails<T>> findExistingIssuesByProjectIssueModel(ProjectIssueModel projectIssueModel) throws AlertException;

}
