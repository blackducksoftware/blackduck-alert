/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.provider.blackduck.tasks;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelationRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.tasks.model.ProjectData;
import com.synopsys.integration.blackduck.api.core.HubView;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.response.AssignedUserGroupView;
import com.synopsys.integration.blackduck.api.generated.view.AssignedUserView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserGroupView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class ProjectSyncTask extends SyncTask<ProjectData> {
    private final Logger logger = LoggerFactory.getLogger(ProjectSyncTask.class);
    private final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor;
    private final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor;
    private final UserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor;

    @Autowired
    public ProjectSyncTask(final TaskScheduler taskScheduler, final BlackDuckProperties blackDuckProperties, final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor,
        final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor, final UserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor) {
        super(taskScheduler, "blackduck-sync-project-task", blackDuckProperties);
        this.blackDuckUserRepositoryAccessor = blackDuckUserRepositoryAccessor;
        this.blackDuckProjectRepositoryAccessor = blackDuckProjectRepositoryAccessor;
        this.userProjectRelationRepositoryAccessor = userProjectRelationRepositoryAccessor;
    }

    @Override
    public List<ProjectView> getHubViews(final HubService hubService) throws IntegrationException {
        return hubService.getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE);
    }

    @Override
    public Map<ProjectData, ? extends HubView> getCurrentData(final List<? extends HubView> hubViews) {
        final List<ProjectView> projectViews = (List<ProjectView>) hubViews;
        final Map<ProjectData, ? extends HubView> projectMap = projectViews.stream().collect(
            Collectors.toMap(projectView -> new ProjectData(projectView.name, StringUtils.trimToEmpty(projectView.description), projectView._meta.href), Function.identity()));
        return projectMap;
    }

    @Override
    public List<? extends DatabaseEntity> getStoredEntities() {
        return blackDuckProjectRepositoryAccessor.readEntities();
    }

    @Override
    public Set<ProjectData> getStoredData(final List<? extends DatabaseEntity> storedEntities) {
        final List<BlackDuckProjectEntity> blackDuckProjectEntities = (List<BlackDuckProjectEntity>) storedEntities;

        final Set<ProjectData> storedGroups = blackDuckProjectEntities.stream().map(blackDuckProjectEntity -> new ProjectData(blackDuckProjectEntity.getName(), blackDuckProjectEntity.getDescription(), blackDuckProjectEntity.getHref()))
                                                  .collect(Collectors.toSet());
        return storedGroups;
    }

    @Override
    public List<BlackDuckProjectEntity> getEntitiesToRemove(final List<? extends DatabaseEntity> storedEntities, final Set<ProjectData> dataToRemove) {
        final List<BlackDuckProjectEntity> blackDuckProjectEntities = (List<BlackDuckProjectEntity>) storedEntities;
        final List<BlackDuckProjectEntity> blackDuckProjectsToRemove = blackDuckProjectEntities.stream()
                                                                           .filter(blackDuckProjectEntity -> {
                                                                                   Optional<ProjectData> found = dataToRemove.stream()
                                                                                                                     .filter(data -> data.getName().equals(blackDuckProjectEntity.getName()))
                                                                                                                     .findFirst();
                                                                                   return found.isPresent();
                                                                               }
                                                                           )
                                                                           .collect(Collectors.toList());
        return blackDuckProjectsToRemove;
    }

    @Override
    public void deleteEntity(final Long id) {
        blackDuckProjectRepositoryAccessor.deleteEntity(id);
        userProjectRelationRepositoryAccessor.deleteRelationByProjectId(id);
    }

    @Override
    public DatabaseEntity createAndSaveEntity(final ProjectData data) {
        return blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity(data.getName(), data.getDescription(), data.getHref()));
    }

    @Override
    public void addRelations(final Map<ProjectData, ? extends HubView> currentDataMap, final List<? extends DatabaseEntity> storedEntities, final HubService hubService) throws IOException, IntegrationException {
        final List<BlackDuckProjectEntity> blackDuckProjectEntities = (List<BlackDuckProjectEntity>) storedEntities;

        for (final Map.Entry<ProjectData, ? extends HubView> entry : currentDataMap.entrySet()) {
            final ProjectData projectData = entry.getKey();
            final ProjectView projectView = (ProjectView) entry.getValue();

            final Optional<BlackDuckProjectEntity> optionalBlackDuckProjectEntity = blackDuckProjectEntities.stream().filter(blackDuckProjectEntity -> blackDuckProjectEntity.getName().equals(projectData.getName())).findFirst();
            final BlackDuckProjectEntity projectEntity = optionalBlackDuckProjectEntity.get();

            final List<AssignedUserView> assignedUsersForThisProject = hubService.getAllResponses(projectView, ProjectView.USERS_LINK_RESPONSE);
            final List<AssignedUserGroupView> assignedGroupsForThisProject = hubService.getAllResponses(projectView, ProjectView.USERGROUPS_LINK_RESPONSE);

            final List<BlackDuckUserEntity> storedUsers = (List<BlackDuckUserEntity>) blackDuckUserRepositoryAccessor.readEntities();

            final Set<BlackDuckUserEntity> userEntitiesForThisProject = new HashSet<>();

            for (final AssignedUserGroupView assignedUserGroupView : assignedGroupsForThisProject) {
                final UserGroupView userGroupView = hubService.getResponse(assignedUserGroupView.group, UserGroupView.class);
                final List<UserView> usersForThisGroup = hubService.getAllResponses(userGroupView, UserGroupView.USERS_LINK_RESPONSE);
                for (final UserView userView : usersForThisGroup) {
                    if (StringUtils.isNotBlank(userView.email)) {
                        final Optional<BlackDuckUserEntity> matchingUser = storedUsers.stream().filter(blackDuckUserEntity -> blackDuckUserEntity.getEmailAddress().equals(userView.email)).findFirst();
                        if (matchingUser.isPresent()) {
                            final BlackDuckUserEntity userEntity = matchingUser.get();
                            userEntitiesForThisProject.add(userEntity);
                        }
                    }
                }
            }
            for (final AssignedUserView assignedUserView : assignedUsersForThisProject) {
                final Optional<BlackDuckUserEntity> matchingUser = storedUsers.stream().filter(blackDuckUserEntity -> blackDuckUserEntity.getEmailAddress().equals(assignedUserView.email)).findFirst();
                if (matchingUser.isPresent()) {
                    final BlackDuckUserEntity userEntity = matchingUser.get();
                    userEntitiesForThisProject.add(userEntity);
                }
            }
            for (final BlackDuckUserEntity blackDuckUserEntity : userEntitiesForThisProject) {
                try {
                    userProjectRelationRepositoryAccessor.addUserProjectRelation(blackDuckUserEntity.getId(), projectEntity.getId());
                } catch (final Exception e) {
                    logger.error("COULD NOT SAVE THIS RELATION {}", e.getMessage());
                }
            }

        }
    }

}
