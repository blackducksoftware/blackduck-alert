/**
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.provider.blackduck.temp_models;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.provider.blackduck.temp_models.message.MessageReason;
import com.synopsys.integration.alert.provider.blackduck.temp_models.message.ProjectMessage;

public class CollapseExample {
    public List<ProjectMessage> collapseMessages(List<ProjectMessage> projectMessages) {
        Map<MessageReason, List<ProjectMessage>> groupedMessages = new EnumMap<>(MessageReason.class);
        for (ProjectMessage projectMessage : projectMessages) {
            groupedMessages.computeIfAbsent(projectMessage.getMessageReason(), ignored -> new LinkedList<>()).add(projectMessage);
        }

        List<ProjectMessage> collapsedProjectMessages = new LinkedList<>();
        for (Map.Entry<MessageReason, List<ProjectMessage>> groupedProjectEntry : groupedMessages.entrySet()) {
            List<ProjectMessage> collapsedMessageGroup = collapseMessages(groupedProjectEntry.getKey(), groupedProjectEntry.getValue());
            collapsedProjectMessages.addAll(collapsedMessageGroup);
        }

        return collapsedProjectMessages;
    }

    // TODO each method returned should be its own class
    private List<ProjectMessage> collapseMessages(MessageReason messageReason, List<ProjectMessage> projectMessageGroup) {
        switch (messageReason) {
            case PROJECT_STATUS:
                return collapseProjectStatusMessages(projectMessageGroup);
            case PROJECT_VERSION_STATUS:
                return collapseProjectVersionStatusMessages(projectMessageGroup);
            case COMPONENT_UPDATE:
                return null;
            case COMPONENT_CONCERN:
                return null;
            default:
                throw new IllegalStateException("Invalid MessageReason: " + messageReason);
        }
    }

    private List<ProjectMessage> collapseProjectStatusMessages(List<ProjectMessage> projectStatusMessages) {
        return collapseProjectMessages(
            projectStatusMessages,
            projectMessage -> projectMessage.getProject().getUrl()
                                  .orElseThrow(() -> new IllegalStateException("Missing project URL"))
        );
    }

    private List<ProjectMessage> collapseProjectVersionStatusMessages(List<ProjectMessage> projectVersionStatusMessages) {
        return collapseProjectMessages(
            projectVersionStatusMessages,
            projectMessage -> projectMessage.getProjectVersion()
                                  .flatMap(LinkableItem::getUrl)
                                  .orElseThrow(() -> new IllegalStateException("Missing project version URL"))
        );
    }

    private List<ProjectMessage> collapseProjectMessages(List<ProjectMessage> projectStatusMessages, Function<ProjectMessage, String> extractKey) {
        Map<String, ProjectMessage> keyToProjectMessage = new LinkedHashMap<>();
        for (ProjectMessage projectMessage : projectStatusMessages) {
            String projectMessageKey = extractKey.apply(projectMessage);
            ProjectMessage previousProjectMessage = keyToProjectMessage.get(projectMessageKey);
            if (null == previousProjectMessage) {
                keyToProjectMessage.put(projectMessageKey, projectMessage);
            } else if (!previousProjectMessage.getOperation().equals(projectMessage.getOperation())) {
                keyToProjectMessage.remove(projectMessageKey);
            }
        }
        return new ArrayList<>(keyToProjectMessage.values());
    }

}
