/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.issue.search;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.channel.api.issue.convert.ProjectMessageToIssueModelTransformer;
import com.synopsys.integration.alert.channel.api.issue.model.IssuePolicyDetails;
import com.synopsys.integration.alert.channel.api.issue.model.IssueVulnerabilityDetails;
import com.synopsys.integration.alert.channel.api.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.MessageReason;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectOperation;
import com.synopsys.integration.function.ThrowingSupplier;

public abstract class IssueTrackerSearcher<T extends Serializable> {
    public final List<ActionableIssueSearchResult<T>> findIssues(ProjectMessage projectMessage) throws AlertException {
        ProviderDetails providerDetails = projectMessage.getProviderDetails();
        LinkableItem project = projectMessage.getProject();

        MessageReason messageReason = projectMessage.getMessageReason();
        boolean isEntireBomDeleted = projectMessage.getOperation()
                                         .filter(ProjectOperation.DELETE::equals)
                                         .isPresent();

        if (MessageReason.PROJECT_STATUS.equals(messageReason)) {
            return findProjectIssues(isEntireBomDeleted, () -> findProjectIssues(providerDetails, project));
        }

        LinkableItem projectVersion = projectMessage.getProjectVersion()
                                          .orElseThrow(() -> new AlertRuntimeException("Missing project version"));
        if (MessageReason.PROJECT_VERSION_STATUS.equals(messageReason)) {
            return findProjectIssues(isEntireBomDeleted, () -> findProjectVersionIssues(providerDetails, project, projectVersion));
        }

        if (MessageReason.COMPONENT_UPDATE.equals(messageReason)) {
            return findIssuesByAllComponents(providerDetails, project, projectVersion, projectMessage.getBomComponents());
        }

        List<ProjectIssueModel> projectIssueModels = ProjectMessageToIssueModelTransformer.convertToIssueModels(projectMessage);

        List<ActionableIssueSearchResult<T>> projectIssueSearchResults = new LinkedList<>();
        for (ProjectIssueModel projectIssueModel : projectIssueModels) {
            ActionableIssueSearchResult<T> searchResult = findIssueByProjectIssueModel(projectIssueModel);
            projectIssueSearchResults.add(searchResult);
        }
        return projectIssueSearchResults;
    }

    protected abstract List<ProjectIssueSearchResult<T>> findProjectIssues(ProviderDetails providerDetails, LinkableItem project) throws AlertException;

    protected abstract List<ProjectIssueSearchResult<T>> findProjectVersionIssues(ProviderDetails providerDetails, LinkableItem project, LinkableItem projectVersion) throws AlertException;

    protected abstract List<ProjectIssueSearchResult<T>> findIssuesByComponent(ProviderDetails providerDetails, LinkableItem project, LinkableItem projectVersion, BomComponentDetails bomComponent) throws AlertException;

    protected abstract List<ExistingIssueDetails<T>> findExistingIssuesByProjectIssueModel(ProjectIssueModel projectIssueModel) throws AlertException;

    private List<ActionableIssueSearchResult<T>> findIssuesByAllComponents(ProviderDetails providerDetails, LinkableItem project, LinkableItem projectVersion, List<BomComponentDetails> bomComponents) throws AlertException {
        List<ProjectIssueSearchResult<T>> componentIssues = new LinkedList<>();
        for (BomComponentDetails bomComponent : bomComponents) {
            List<ProjectIssueSearchResult<T>> issuesByComponent = findIssuesByComponent(providerDetails, project, projectVersion, bomComponent);
            componentIssues.addAll(issuesByComponent);
        }
        return componentIssues
                   .stream()
                   .map(this::convertToUpdateResult)
                   .collect(Collectors.toList());
    }

    private ActionableIssueSearchResult<T> findIssueByProjectIssueModel(ProjectIssueModel projectIssueModel) throws AlertException {
        ExistingIssueDetails<T> existingIssue = null;
        ItemOperation searchResultOperation = ItemOperation.UPDATE;

        List<ExistingIssueDetails<T>> existingIssues = findExistingIssuesByProjectIssueModel(projectIssueModel);
        int foundIssuesCount = existingIssues.size();

        if (foundIssuesCount == 1) {
            existingIssue = existingIssues.get(0);

            Optional<ItemOperation> policyOperation = projectIssueModel.getPolicyDetails().map(IssuePolicyDetails::getOperation);
            Optional<IssueVulnerabilityDetails> optionalVulnerabilityDetails = projectIssueModel.getVulnerabilityDetails();
            if (policyOperation.isPresent()) {
                searchResultOperation = policyOperation.get();
            } else if (optionalVulnerabilityDetails.isPresent()) {
                IssueVulnerabilityDetails issueVulnerabilityDetails = optionalVulnerabilityDetails.get();
                // TODO when ExistingIssueDetails has information about issue-status, use that to make a better choice of ItemOperation here.
                searchResultOperation = issueVulnerabilityDetails.areAllComponentVulnerabilitiesRemediated() ? ItemOperation.DELETE : ItemOperation.ADD;
            }
        } else if (foundIssuesCount > 1) {
            throw new AlertException("Expect to find a unique issue, but more than one was found");
        } else {
            searchResultOperation = ItemOperation.ADD;
        }
        return new ActionableIssueSearchResult<>(existingIssue, projectIssueModel, searchResultOperation);
    }

    private List<ActionableIssueSearchResult<T>> findProjectIssues(boolean isEntireBomDeleted, ThrowingSupplier<List<ProjectIssueSearchResult<T>>, AlertException> find) throws AlertException {
        if (isEntireBomDeleted) {
            return find.get()
                       .stream()
                       .map(this::convertToDeleteResult)
                       .collect(Collectors.toList());
        }
        return List.of();
    }

    private ActionableIssueSearchResult<T> convertToDeleteResult(ProjectIssueSearchResult<T> projectIssueSearchResult) {
        return convertToOperationResult(projectIssueSearchResult, ItemOperation.DELETE);
    }

    private ActionableIssueSearchResult<T> convertToUpdateResult(ProjectIssueSearchResult<T> projectIssueSearchResult) {
        return convertToOperationResult(projectIssueSearchResult, ItemOperation.UPDATE);
    }

    private ActionableIssueSearchResult<T> convertToOperationResult(ProjectIssueSearchResult<T> projectIssueSearchResult, ItemOperation operation) {
        return new ActionableIssueSearchResult<>(projectIssueSearchResult.getExistingIssueDetails(), projectIssueSearchResult.getProjectIssueModel(), operation);
    }

}
