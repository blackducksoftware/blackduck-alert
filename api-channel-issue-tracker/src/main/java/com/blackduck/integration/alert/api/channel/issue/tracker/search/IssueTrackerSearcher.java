/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker.search;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackduck.integration.alert.api.channel.issue.tracker.convert.ProjectMessageToIssueModelTransformer;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.enumeration.IssueStatus;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueComponentUnknownVersionDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssuePolicyDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueVulnerabilityDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.common.model.exception.AlertRuntimeException;
import com.blackduck.integration.alert.common.enumeration.ItemOperation;
import com.blackduck.integration.alert.common.message.model.LinkableItem;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderDetails;
import com.blackduck.integration.alert.api.processor.extract.model.project.BomComponentDetails;
import com.blackduck.integration.alert.api.processor.extract.model.project.MessageReason;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectMessage;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectOperation;
import com.blackduck.integration.function.ThrowingSupplier;

public class IssueTrackerSearcher<T extends Serializable> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ProjectIssueFinder<T> projectIssueFinder;
    private final ProjectVersionIssueFinder<T> projectVersionIssueFinder;
    private final ProjectVersionComponentIssueFinder<T> projectVersionComponentIssueFinder;
    private final ExactIssueFinder<T> exactIssueFinder;
    private final ProjectMessageToIssueModelTransformer modelTransformer;

    public IssueTrackerSearcher(
        ProjectIssueFinder<T> projectIssueFinder,
        ProjectVersionIssueFinder<T> projectVersionIssueFinder,
        ProjectVersionComponentIssueFinder<T> projectVersionComponentIssueFinder,
        ExactIssueFinder<T> exactIssueFinder,
        ProjectMessageToIssueModelTransformer modelTransformer
    ) {
        this.projectIssueFinder = projectIssueFinder;
        this.projectVersionIssueFinder = projectVersionIssueFinder;
        this.projectVersionComponentIssueFinder = projectVersionComponentIssueFinder;
        this.exactIssueFinder = exactIssueFinder;
        this.modelTransformer = modelTransformer;
    }

    public final List<ActionableIssueSearchResult<T>> findIssues(ProjectMessage projectMessage) throws AlertException {
        ProviderDetails providerDetails = projectMessage.getProviderDetails();
        LinkableItem project = projectMessage.getProject();

        MessageReason messageReason = projectMessage.getMessageReason();
        boolean isEntireBomDeleted = projectMessage.getOperation()
            .filter(ProjectOperation.DELETE::equals)
            .isPresent();

        if (MessageReason.PROJECT_STATUS.equals(messageReason)) {
            return findProjectIssues(isEntireBomDeleted, () -> projectIssueFinder.findProjectIssues(providerDetails, project));
        }

        LinkableItem projectVersion = projectMessage.getProjectVersion()
            .orElseThrow(() -> new AlertRuntimeException("Missing project version"));
        if (MessageReason.PROJECT_VERSION_STATUS.equals(messageReason)) {
            return findProjectIssues(isEntireBomDeleted, () -> projectVersionIssueFinder.findProjectVersionIssues(providerDetails, project, projectVersion));
        }

        if (MessageReason.COMPONENT_UPDATE.equals(messageReason)) {
            return findIssuesByAllComponents(providerDetails, project, projectVersion, projectMessage.getBomComponents());
        }

        List<ProjectIssueModel> projectIssueModels = modelTransformer.convertToIssueModels(projectMessage);

        List<ActionableIssueSearchResult<T>> projectIssueSearchResults = new LinkedList<>();
        for (ProjectIssueModel projectIssueModel : projectIssueModels) {
            ActionableIssueSearchResult<T> searchResult = findIssueByProjectIssueModel(projectIssueModel);
            if (searchResult.getExistingIssueDetails().isEmpty() && isOnlyDeleteOperation(projectIssueModel)) {
                logger.debug("Ignoring component-level notification for issue-tracker because no matching issue(s) existed and it only contained DELETE operations");
            } else {
                projectIssueSearchResults.add(searchResult);
            }
        }
        return projectIssueSearchResults;
    }

    private List<ActionableIssueSearchResult<T>> findIssuesByAllComponents(ProviderDetails providerDetails, LinkableItem project, LinkableItem projectVersion, List<BomComponentDetails> bomComponents) throws AlertException {
        List<IssueTrackerSearchResult<T>> componentIssues = new LinkedList<>();
        for (BomComponentDetails bomComponent : bomComponents) {
            IssueTrackerSearchResult<T> issuesByComponent = projectVersionComponentIssueFinder.findIssuesByComponent(providerDetails, project, projectVersion, bomComponent);
            componentIssues.add(issuesByComponent);
        }
        return componentIssues
            .stream()
            .map(this::convertToUpdateResult)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    private ActionableIssueSearchResult<T> findIssueByProjectIssueModel(ProjectIssueModel projectIssueModel) throws AlertException {
        ExistingIssueDetails<T> existingIssue = null;
        ItemOperation searchResultOperation = ItemOperation.UPDATE;

        IssueTrackerSearchResult<T> searchResponse = exactIssueFinder.findExistingIssuesByProjectIssueModel(projectIssueModel);
        List<ProjectIssueSearchResult<T>> existingIssues = searchResponse.getSearchResults();
        int foundIssuesCount = existingIssues.size();

        if (foundIssuesCount == 1) {
            existingIssue = existingIssues.get(0).getExistingIssueDetails();

            Optional<ItemOperation> policyOperation = projectIssueModel.getPolicyDetails().map(IssuePolicyDetails::getOperation);
            Optional<IssueVulnerabilityDetails> optionalVulnerabilityDetails = projectIssueModel.getVulnerabilityDetails();
            Optional<ItemOperation> componentUnknownOperation = projectIssueModel.getComponentUnknownVersionDetails().map(IssueComponentUnknownVersionDetails::getItemOperation);
            if (policyOperation.isPresent()) {
                searchResultOperation = policyOperation.get();
            } else if (optionalVulnerabilityDetails.isPresent()) {
                IssueVulnerabilityDetails issueVulnerabilityDetails = optionalVulnerabilityDetails.get();
                searchResultOperation = findVulnerabilitySearchResultOperation(existingIssue, issueVulnerabilityDetails);
            } else if (componentUnknownOperation.isPresent()) {
                searchResultOperation = componentUnknownOperation.get();
            }
        } else if (foundIssuesCount > 1) {
            Set<String> issueKeys = existingIssues.stream()
                .map(ProjectIssueSearchResult::getExistingIssueDetails)
                .map(ExistingIssueDetails::getIssueKey)
                .collect(Collectors.toSet());
            String issueKeyString = StringUtils.join(issueKeys, ", ");
            throw new AlertException("Expected to find a unique issue, but more than one was found. " + issueKeyString);
        } else {
            searchResultOperation = ItemOperation.ADD;
        }
        return new ActionableIssueSearchResult<>(existingIssue, projectIssueModel, searchResponse.getSearchQuery(), searchResultOperation);
    }

    private ItemOperation findVulnerabilitySearchResultOperation(ExistingIssueDetails<T> existingIssue, IssueVulnerabilityDetails issueVulnerabilityDetails) {
        ItemOperation searchResultOperation;
        boolean isResolvableOrUnknown = IssueStatus.RESOLVABLE.equals(existingIssue.getIssueStatus()) || IssueStatus.UNKNOWN.equals(existingIssue.getIssueStatus());
        boolean isReopenableOrUnknown = IssueStatus.REOPENABLE.equals(existingIssue.getIssueStatus()) || IssueStatus.UNKNOWN.equals(existingIssue.getIssueStatus());
        if (issueVulnerabilityDetails.areAllComponentVulnerabilitiesRemediated() && isResolvableOrUnknown) {
            searchResultOperation = ItemOperation.DELETE;
        } else if (!issueVulnerabilityDetails.areAllComponentVulnerabilitiesRemediated() && isReopenableOrUnknown) {
            searchResultOperation = ItemOperation.ADD;
        } else {
            searchResultOperation = ItemOperation.UPDATE;
        }
        return searchResultOperation;
    }

    private List<ActionableIssueSearchResult<T>> findProjectIssues(boolean isEntireBomDeleted, ThrowingSupplier<IssueTrackerSearchResult<T>, AlertException> find)
        throws AlertException {
        if (isEntireBomDeleted) {
            return convertToDeleteResult(find.get());
        }
        logger.debug("Ignoring project-level notification for issue-tracker because no action would be taken");
        return List.of();
    }

    private List<ActionableIssueSearchResult<T>> convertToDeleteResult(IssueTrackerSearchResult<T> searchResponse) {
        return searchResponse.getSearchResults().stream()
            .map(projectIssueSearchResult -> convertToOperationResult(searchResponse.getSearchQuery(), projectIssueSearchResult, ItemOperation.DELETE))
            .collect(Collectors.toList());
    }

    private List<ActionableIssueSearchResult<T>> convertToUpdateResult(IssueTrackerSearchResult<T> searchResponse) {
        return searchResponse.getSearchResults().stream()
            .map(projectIssueSearchResult -> convertToOperationResult(searchResponse.getSearchQuery(), projectIssueSearchResult, ItemOperation.UPDATE))
            .collect(Collectors.toList());
    }

    private ActionableIssueSearchResult<T> convertToOperationResult(String searchQuery, ProjectIssueSearchResult<T> projectIssueSearchResult, ItemOperation operation) {
        return new ActionableIssueSearchResult<>(
            projectIssueSearchResult.getExistingIssueDetails(),
            projectIssueSearchResult.getProjectIssueModel(),
            searchQuery,
            operation
        );
    }

    private boolean isOnlyDeleteOperation(ProjectIssueModel projectIssueModel) {
        boolean isPolicyDelete = projectIssueModel.getPolicyDetails()
            .map(IssuePolicyDetails::getOperation)
            .filter(ItemOperation.DELETE::equals)
            .isPresent();

        boolean isEstimatedRiskDelete = projectIssueModel.getComponentUnknownVersionDetails()
            .map(IssueComponentUnknownVersionDetails::getItemOperation)
            .filter(ItemOperation.DELETE::equals)
            .isPresent();

        boolean isVulnerabilityDelete = false;
        Optional<IssueVulnerabilityDetails> optionalVulnDetails = projectIssueModel.getVulnerabilityDetails();
        if (optionalVulnDetails.isPresent()) {
            IssueVulnerabilityDetails vulnDetails = optionalVulnDetails.get();
            boolean allVulnsRemediated = vulnDetails.areAllComponentVulnerabilitiesRemediated();
            boolean hasDeletions = !vulnDetails.getVulnerabilitiesDeleted().isEmpty();
            boolean doesNotHaveAdditions = vulnDetails.getVulnerabilitiesAdded().isEmpty();
            boolean doesNotHaveUpdates = vulnDetails.getVulnerabilitiesUpdated().isEmpty();
            isVulnerabilityDelete = allVulnsRemediated || (hasDeletions && doesNotHaveAdditions && doesNotHaveUpdates);
        }

        return isPolicyDelete || isVulnerabilityDelete || isEstimatedRiskDelete;
    }

}
