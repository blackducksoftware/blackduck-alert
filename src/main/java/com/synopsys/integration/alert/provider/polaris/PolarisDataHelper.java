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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.polaris.common.api.generated.common.BranchV0;
import com.synopsys.integration.polaris.common.api.generated.common.ProjectV0;
import com.synopsys.integration.polaris.common.model.QueryIssue;
import com.synopsys.integration.polaris.common.service.BranchService;
import com.synopsys.integration.polaris.common.service.ProjectService;

@Component
public class PolarisDataHelper {
    public String getProjectName(final ProjectV0 project) {
        return project
                   .getAttributes()
                   .getName();
    }

    public String getProjectHref(final ProjectV0 project) {
        return project
                   .getLinks()
                   .getSelf()
                   .getHref();
    }

    public Optional<ProjectV0> getProjectByHrefOrName(final Set<ProjectV0> projects, final String href, final String name, final ProjectService projectService) throws IntegrationException {
        final Optional<ProjectV0> optionalProjectV0 = projects
                                                          .stream()
                                                          .filter(p -> href.equals(getProjectHref(p)))
                                                          .findFirst();
        if (optionalProjectV0.isPresent()) {
            return optionalProjectV0;
        }
        return projectService.getProjectByName(name);
    }

    public List<String> getBranchesIdsForProject(final Map<ProjectV0, List<BranchV0>> projectToBranchMappings, final ProjectV0 project, final BranchService branchService) throws IntegrationException {
        if (projectToBranchMappings.containsKey(project)) {
            return projectToBranchMappings.get(project)
                       .stream()
                       .map(BranchV0::getId)
                       .collect(Collectors.toList());
        }
        return branchService.getBranchesForProject(project.getId())
                   .stream()
                   .map(BranchV0::getId)
                   .collect(Collectors.toList());
    }

    public final Map<String, Integer> mapIssueTypeToCount(final List<QueryIssue> queryIssues) {
        final Map<String, Integer> issueTypeCounts = new HashMap<>();
        for (final QueryIssue queryIssue : queryIssues) {
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

}
