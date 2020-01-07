/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.provider.polaris;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.persistence.model.PolarisIssueModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.polaris.common.api.auth.model.group.GroupResource;
import com.synopsys.integration.polaris.common.api.auth.model.role.RoleAttributes;
import com.synopsys.integration.polaris.common.api.auth.model.role.RoleResource;
import com.synopsys.integration.polaris.common.api.auth.model.role.assignments.RoleAssignmentResource;
import com.synopsys.integration.polaris.common.api.auth.model.role.assignments.RoleAssignmentResources;
import com.synopsys.integration.polaris.common.api.auth.model.user.UserResource;
import com.synopsys.integration.polaris.common.api.common.model.branch.BranchV0Resource;
import com.synopsys.integration.polaris.common.api.common.model.project.ProjectV0Resource;
import com.synopsys.integration.polaris.common.api.query.model.issue.IssueV0Resource;
import com.synopsys.integration.polaris.common.api.query.model.issue.type.IssueTypeV0Attributes;
import com.synopsys.integration.polaris.common.api.query.model.issue.type.IssueTypeV0Resource;
import com.synopsys.integration.polaris.common.model.IssueResourcesSingle;
import com.synopsys.integration.polaris.common.service.IssueService;
import com.synopsys.integration.polaris.common.service.RoleAssignmentService;
import com.synopsys.integration.polaris.common.service.UserService;

public class PolarisApiHelper {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final IssueService issueService;
    private final RoleAssignmentService roleAssignmentService;
    private final UserService userService;

    public PolarisApiHelper(final IssueService issueService, final RoleAssignmentService roleAssignmentService, final UserService userService) {
        this.issueService = issueService;
        this.roleAssignmentService = roleAssignmentService;
        this.userService = userService;
    }

    public Set<PolarisIssueModel> createIssueModelsForProject(final String projectId, final String projectName, final List<String> branchIds) {
        final Set<PolarisIssueModel> issuesForProjectFromServer = new HashSet<>();
        for (final String branchId : branchIds) {
            try {
                final List<IssueV0Resource> foundIssues = issueService.getIssuesForProjectAndBranch(projectId, branchId);
                final Map<String, Integer> issueTypeCounts = mapIssueTypeToCount(projectId, branchId, foundIssues);

                for (final Map.Entry<String, Integer> issueTypeEntry : issueTypeCounts.entrySet()) {
                    final PolarisIssueModel newIssue = PolarisIssueModel.createNewIssue(issueTypeEntry.getKey(), issueTypeEntry.getValue());
                    issuesForProjectFromServer.add(newIssue);
                }
            } catch (final IntegrationException e) {
                logger.error("Problem getting issues from Polaris: {}", projectName, e);
            }
        }
        return issuesForProjectFromServer;
    }

    public List<String> getBranchesIdsForProject(final List<BranchV0Resource> branches) {
        return branches
                   .stream()
                   .map(BranchV0Resource::getId)
                   .collect(Collectors.toList());
    }

    public Set<String> getAllEmailsForProject(final ProjectV0Resource project) throws IntegrationException {
        List<UserResource> allUsers = null;
        final RoleAssignmentResources projectRoleAssignments = roleAssignmentService.getRoleAssignmentsForProjectWithIncluded(project.getId(),
            RoleAssignmentService.INCLUDE_USERS, RoleAssignmentService.INCLUDE_GROUPS);

        final Set<String> emails = new HashSet<>();
        for (final RoleAssignmentResource roleAssignment : projectRoleAssignments.getData()) {
            final Optional<String> optionalUserEmail = getEmailForRoleAssignedUser(projectRoleAssignments, roleAssignment);
            if (optionalUserEmail.isPresent()) {
                emails.add(optionalUserEmail.get());
            } else {
                if (null == allUsers) {
                    allUsers = userService.getAllUsers();
                }
                final Set<String> groupEmails = getGroupEmailsForRoleAssignedUser(projectRoleAssignments, roleAssignment, allUsers);
                emails.addAll(groupEmails);
            }
        }
        return emails;
    }

    public Optional<String> getAdminEmailForProject(final ProjectV0Resource project) throws IntegrationException {
        final RoleAssignmentResources projectRoleAssignements = roleAssignmentService.getRoleAssignmentsForProjectWithIncluded(project.getId(), RoleAssignmentService.INCLUDE_USERS, RoleAssignmentService.INCLUDE_ROLES);
        for (final RoleAssignmentResource roleAssignment : projectRoleAssignements.getData()) {
            final Optional<String> optionalRoleName = roleAssignmentService.getRoleFromPopulatedRoleAssignments(projectRoleAssignements, roleAssignment)
                                                          .map(RoleResource::getAttributes)
                                                          .map(RoleAttributes::getRolename)
                                                          .filter(roleName -> roleName.equals(RoleAttributes.ROLE_ADMINISTRATOR));
            if (optionalRoleName.isPresent()) {
                final Optional<String> optionalEmail = getEmailForRoleAssignedUser(projectRoleAssignements, roleAssignment);
                if (optionalEmail.isPresent()) {
                    return optionalEmail;
                }
            }
        }
        return Optional.empty();
    }

    public Optional<String> createLinkToProject(final String polarisUrl, final String projectId) {
        if (StringUtils.isNotBlank(polarisUrl) && StringUtils.isNotBlank(projectId)) {
            final String projectLink = String.format("%s/projects/%s", polarisUrl, projectId);
            return Optional.of(projectLink);
        }
        return Optional.empty();
    }

    private Map<String, Integer> mapIssueTypeToCount(final String projectId, final String branchId, final List<IssueV0Resource> queryIssues) throws IntegrationException {
        final Map<String, Integer> issueTypeCounts = new HashMap<>();
        final Map<String, String> issueTypeMap = new HashMap<>();
        for (final IssueV0Resource queryIssue : queryIssues) {
            final String subTool = queryIssue.getAttributes().getSubTool();
            final String issueType;
            if (issueTypeMap.containsKey(subTool)) {
                issueType = issueTypeMap.get(subTool);
            } else {
                final String issueKey = queryIssue.getAttributes().getIssueKey();
                final IssueResourcesSingle populatedIssueResource = issueService.getIssueForProjectBranchAndIssueKeyWithDefaultIncluded(projectId, branchId, issueKey);
                issueType = issueService
                                .getIssueTypeFromPopulatedIssueResources(populatedIssueResource)
                                .map(IssueTypeV0Resource::getAttributes)
                                .map(IssueTypeV0Attributes::getName)
                                .orElse(subTool);
                issueTypeMap.put(subTool, issueType);
            }
            if (!issueTypeCounts.containsKey(issueType)) {
                issueTypeCounts.put(issueType, 0);
            }
            final Integer tempCount = issueTypeCounts.get(issueType);
            issueTypeCounts.put(issueType, tempCount + 1);
        }
        return issueTypeCounts;
    }

    private Optional<String> getEmailForRoleAssignedUser(final RoleAssignmentResources populatedRoleAssignments, final RoleAssignmentResource roleAssignment) throws IntegrationException {
        final Optional<UserResource> user = roleAssignmentService.getUserFromPopulatedRoleAssignments(populatedRoleAssignments, roleAssignment);
        if (user.isPresent()) {
            return userService.getEmailForUser(user.get());
        }
        return Optional.empty();
    }

    private Set<String> getGroupEmailsForRoleAssignedUser(final RoleAssignmentResources populatedRoleAssignments, final RoleAssignmentResource roleAssignment, final List<UserResource> allUsers) throws IntegrationException {
        final Set<String> groupEmails = new HashSet<>();
        final Optional<GroupResource> optionalGroup = roleAssignmentService.getGroupFromPopulatedRoleAssignments(populatedRoleAssignments, roleAssignment);
        if (optionalGroup.isPresent()) {
            final Set<UserResource> usersInGroup = userService.getUsersForGroup(allUsers, optionalGroup.get());
            for (final UserResource userInGroup : usersInGroup) {
                userService.getEmailForUser(userInGroup).ifPresent(groupEmails::add);
            }
        }
        return groupEmails;
    }

}
