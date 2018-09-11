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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckGroupEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckGroupRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserGroupRelationRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.model.BlackDuckGroup;
import com.synopsys.integration.blackduck.api.core.HubView;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.UserGroupView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class GroupSyncTask extends SyncTask<BlackDuckGroup> {
    private final Logger logger = LoggerFactory.getLogger(GroupSyncTask.class);
    private final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor;
    private final UserGroupRelationRepositoryAccessor userGroupRelationRepositoryAccessor;

    @Autowired
    public GroupSyncTask(final TaskScheduler taskScheduler, final BlackDuckProperties blackDuckProperties, final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor,
        final BlackDuckGroupRepositoryAccessor blackDuckGroupRepositoryAccessor,
        final UserGroupRelationRepositoryAccessor userGroupRelationRepositoryAccessor) {
        super(taskScheduler, "blackduck-sync-group-task", blackDuckProperties, blackDuckGroupRepositoryAccessor);
        this.blackDuckUserRepositoryAccessor = blackDuckUserRepositoryAccessor;
        this.userGroupRelationRepositoryAccessor = userGroupRelationRepositoryAccessor;
    }

    @Override
    public List<UserGroupView> getHubViews(final HubService hubService) throws IntegrationException {
        return hubService.getAllResponses(ApiDiscovery.USERGROUPS_LINK_RESPONSE);
    }

    @Override
    public Map<BlackDuckGroup, ? extends HubView> getCurrentData(final List<? extends HubView> hubViews) {
        final List<UserGroupView> userGroupViews = (List<UserGroupView>) hubViews;
        final Map<BlackDuckGroup, ? extends HubView> groupMap = userGroupViews.stream().collect(Collectors.toMap(groupView -> new BlackDuckGroup(groupView.name, groupView.active, groupView._meta.href), Function.identity()));
        return groupMap;
    }

    @Override
    public Set<BlackDuckGroup> getStoredData(final List<? extends DatabaseEntity> storedEntities) {
        final List<BlackDuckGroupEntity> blackDuckGroupEntities = (List<BlackDuckGroupEntity>) storedEntities;

        final Set<BlackDuckGroup> storedGroups = blackDuckGroupEntities.stream().map(blackDuckGroupEntity -> new BlackDuckGroup(blackDuckGroupEntity.getName(), blackDuckGroupEntity.getActive(), blackDuckGroupEntity.getHref()))
                                                     .collect(Collectors.toSet());
        return storedGroups;
    }

    @Override
    public List<BlackDuckGroupEntity> getEntitiesToRemove(final List<? extends DatabaseEntity> storedEntities, final Set<BlackDuckGroup> dataToRemove) {
        final List<BlackDuckGroupEntity> blackDuckGroupEntities = (List<BlackDuckGroupEntity>) storedEntities;
        final List<BlackDuckGroupEntity> blackDuckGroupsToRemove = blackDuckGroupEntities.stream()
                                                                       .filter(blackDuckGroupEntity -> isEntityWithNamePresent(dataToRemove, blackDuckGroupEntity))
                                                                       .collect(Collectors.toList());
        return blackDuckGroupsToRemove;
    }

    private Boolean isEntityWithNamePresent(final Set<BlackDuckGroup> groupDataSet, final BlackDuckGroupEntity blackDuckGroupEntity) {
        return groupDataSet.stream()
                   .filter(groupData -> groupData.getName().equals(blackDuckGroupEntity.getName()))
                   .findFirst().isPresent();
    }

    @Override
    public DatabaseEntity createEntity(final BlackDuckGroup data) {
        return new BlackDuckGroupEntity(data.getName(), data.getActive(), data.getHref());
    }

    @Override
    public void addRelations(final Map<BlackDuckGroup, ? extends HubView> currentDataMap, final List<? extends DatabaseEntity> storedEntities, final HubService hubService) throws IOException, IntegrationException {
        final List<BlackDuckGroupEntity> blackDuckGroupEntities = (List<BlackDuckGroupEntity>) storedEntities;

        final List<Pair<Long, Long>> newRelations = new ArrayList<>();
        for (final Map.Entry<BlackDuckGroup, ? extends HubView> entry : currentDataMap.entrySet()) {
            final BlackDuckGroup blackDuckGroup = entry.getKey();
            final UserGroupView userGroupView = (UserGroupView) entry.getValue();
            final Optional<BlackDuckGroupEntity> optionalBlackDuckGroupEntity = blackDuckGroupEntities.stream().filter(blackDuckGroupEntity -> blackDuckGroupEntity.getName().equals(blackDuckGroup.getName())).findFirst();
            final BlackDuckGroupEntity groupEntity = optionalBlackDuckGroupEntity.get();

            final List<BlackDuckUserEntity> storedUsers = (List<BlackDuckUserEntity>) blackDuckUserRepositoryAccessor.readEntities();

            final Set<BlackDuckUserEntity> userEntitiesForThisGroup = new HashSet<>();
            final List<UserView> usersForThisGroup = hubService.getAllResponses(userGroupView, UserGroupView.USERS_LINK_RESPONSE);
            for (final UserView userView : usersForThisGroup) {
                if (StringUtils.isNotBlank(userView.email)) {
                    final Optional<BlackDuckUserEntity> matchingUser = storedUsers.stream().filter(blackDuckUserEntity -> blackDuckUserEntity.getEmailAddress().equals(userView.email)).findFirst();
                    if (matchingUser.isPresent()) {
                        // If the user is not present, it will be in the next run of the task and we will create the relation then
                        final BlackDuckUserEntity userEntity = matchingUser.get();
                        userEntitiesForThisGroup.add(userEntity);
                    }
                }
            }
            for (final BlackDuckUserEntity userEntity : userEntitiesForThisGroup) {
                newRelations.add(Pair.of(userEntity.getId(), groupEntity.getId()));
            }
        }
        userGroupRelationRepositoryAccessor.deleteAllRelations();
        logger.info("User to group relationships {}", newRelations.size());
        for (final Pair<Long, Long> relation : newRelations) {
            try {
                userGroupRelationRepositoryAccessor.addUserGroupRelation(relation.getKey(), relation.getValue());
            } catch (final Exception e) {
                logger.error("Could not save the relation from user {} to group {}: {}", relation.getKey(), relation.getValue(), e.getMessage());
            }
        }
    }

}
