/*
 * channel-api
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
package com.synopys.integration.alert.channel.api.issue;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.channel.issuetracker.message.AlertIssueOrigin;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ContentKey;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopys.integration.alert.channel.api.issue.model.ProjectIssueModel;

public class AlertIssueOriginCreator {
    public AlertIssueOrigin createIssueOrigin(ProjectIssueModel projectIssueModel) {
        LinkableItem provider = projectIssueModel.getProvider();
        LinkableItem project = projectIssueModel.getProject();
        LinkableItem projectVersion = projectIssueModel.getProjectVersion()
                                          .orElseThrow(() -> new AlertRuntimeException("Missing BlackDuck project-version"));
        ContentKey providerContentKey = new ContentKey(
            provider.getLabel(),
            null,
            project.getLabel(),
            project.getValue(),
            projectVersion.getLabel(),
            projectVersion.getValue(),
            ItemOperation.UPDATE
        );

        BomComponentDetails bomComponent = projectIssueModel.getBomComponent();
        ComponentConcern arbitraryComponentConcern = bomComponent.getComponentConcerns()
                                                         .stream()
                                                         .findAny()
                                                         .orElseThrow(() -> new AlertRuntimeException("Missing component-concern"));
        String categoryString = StringUtils.capitalize(arbitraryComponentConcern.getType().name().toLowerCase());

        ComponentItem componentItem;
        try {
            componentItem = new ComponentItem.Builder()
                                .applyCategory(categoryString)
                                .applyOperation(arbitraryComponentConcern.getOperation())
                                .applyComponentData(bomComponent.getComponent())
                                .applySubComponent(bomComponent.getComponentVersion().orElse(null))
                                .applyCategoryItem(categoryString, arbitraryComponentConcern.getName())
                                .applyNotificationId(0L)
                                .build();
        } catch (AlertException e) {
            throw new AlertRuntimeException(e);
        }
        return new AlertIssueOrigin(providerContentKey, componentItem);
    }

}
