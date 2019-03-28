/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
import com.synopsys.integration.polaris.common.api.common.branch.BranchV0Resource;
import com.synopsys.integration.polaris.common.api.common.project.ProjectV0Resource;
import com.synopsys.integration.polaris.common.model.QueryIssueResource;
import com.synopsys.integration.polaris.common.service.IssueService;
import com.synopsys.integration.polaris.common.service.RoleAssignmentsService;
import com.synopsys.integration.polaris.common.service.UserService;

public class PolarisApiHelper {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final IssueService issueService;
    private final RoleAssignmentsService roleAssignmentsService;
    private final UserService userService;

    public PolarisApiHelper(final IssueService issueService, final RoleAssignmentsService roleAssignmentsService, final UserService userService) {
        this.issueService = issueService;
        this.roleAssignmentsService = roleAssignmentsService;
        this.userService = userService;
    }

    public Set<PolarisIssueModel> createIssueModelsForProject(final String projectId, final String projectName, final List<String> branchIds) {
        final Set<PolarisIssueModel> issuesForProjectFromServer = new HashSet<>();
        for (final String branchId : branchIds) {
            try {
                final List<QueryIssueResource> foundIssues = issueService.getIssuesForProjectAndBranch(projectId, branchId);
                final Map<String, Integer> issueTypeCounts = mapIssueTypeToCount(foundIssues);

                for (final Map.Entry<String, Integer> issueTypeEntry : issueTypeCounts.entrySet()) {
                    final PolarisIssueModel newIssue = new PolarisIssueModel(issueTypeEntry.getKey(), 0, issueTypeEntry.getValue());
                    issuesForProjectFromServer.add(newIssue);
                }
            } catch (final IntegrationException e) {
                logger.error("Problem getting issues from Polaris: {}", projectName, e);
                continue;
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
        final RoleAssignmentResources projectRoleAssignements = roleAssignmentsService.getRoleAssignmentsForProjectWithIncluded(project.getId(),
            RoleAssignmentsService.INCLUDE_USERS, RoleAssignmentsService.INCLUDE_GROUPS);

        final Set<String> emails = new HashSet<>();
        for (final RoleAssignmentResource roleAssignment : projectRoleAssignements.getData()) {
            final Optional<String> optionalUserEmail = getEmailForRoleAssignedUser(projectRoleAssignements, roleAssignment);
            if (optionalUserEmail.isPresent()) {
                emails.add(optionalUserEmail.get());
            } else {
                if (null == allUsers) {
                    allUsers = userService.getAllUsers();
                }
                final Set<String> groupEmails = getGroupEmailsForRoleAssignedUser(projectRoleAssignements, roleAssignment, allUsers);
                emails.addAll(groupEmails);
            }
        }
        return emails;
    }

    public Optional<String> getAdminEmailForProject(final ProjectV0Resource project) throws IntegrationException {
        final RoleAssignmentResources projectRoleAssignements = roleAssignmentsService.getRoleAssignmentsForProjectWithIncluded(project.getId(), RoleAssignmentsService.INCLUDE_USERS, RoleAssignmentsService.INCLUDE_ROLES);
        for (final RoleAssignmentResource roleAssignment : projectRoleAssignements.getData()) {
            final Optional<String> optionalRoleName = roleAssignmentsService.getRoleFromPopulatedRoleAssignments(projectRoleAssignements, roleAssignment)
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

    private Map<String, Integer> mapIssueTypeToCount(final List<QueryIssueResource> queryIssues) {
        final Map<String, Integer> issueTypeCounts = new HashMap<>();
        for (final QueryIssueResource queryIssue : queryIssues) {
            // FIXME issue type is not the same as issue key
            final String issueType = queryIssue.getAttributes().getSubTool();
            if (!issueTypeCounts.containsKey(issueType)) {
                issueTypeCounts.put(issueType, 0);
            }
            final Integer tempCount = issueTypeCounts.get(issueType);
            issueTypeCounts.put(issueType, tempCount + 1);
        }
        return issueTypeCounts;
    }

    private Optional<String> getEmailForRoleAssignedUser(final RoleAssignmentResources populatedRoleAssignments, final RoleAssignmentResource roleAssignment) throws IntegrationException {
        final Optional<UserResource> user = roleAssignmentsService.getUserFromPopulatedRoleAssignments(populatedRoleAssignments, roleAssignment);
        if (user.isPresent()) {
            return userService.getEmailForUser(user.get());
        }
        return Optional.empty();
    }

    private Set<String> getGroupEmailsForRoleAssignedUser(final RoleAssignmentResources populatedRoleAssignments, final RoleAssignmentResource roleAssignment, final List<UserResource> allUsers) throws IntegrationException {
        final Set<String> groupEmails = new HashSet<>();
        final Optional<GroupResource> optionalGroup = roleAssignmentsService.getGroupFromPopulatedRoleAssignments(populatedRoleAssignments, roleAssignment);
        if (optionalGroup.isPresent()) {
            final Set<UserResource> usersInGroup = userService.getUsersForGroup(allUsers, optionalGroup.get());
            for (final UserResource userInGroup : usersInGroup) {
                userService.getEmailForUser(userInGroup).ifPresent(groupEmails::add);
            }
        }
        return groupEmails;
    }

}
