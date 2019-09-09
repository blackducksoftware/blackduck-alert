/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.provider.blackduck.collector;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ComponentItemPriority;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonField;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonFieldAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckContent;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BlackDuckProjectVersionCollector extends BlackDuckCollector {
    private static final String CATEGORY_TYPE = "Project Event";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public BlackDuckProjectVersionCollector(JsonExtractor jsonExtractor, BlackDuckProperties blackDuckProperties) {
        super(jsonExtractor, List.of(BlackDuckContent.PROJECT, BlackDuckContent.PROJECT_VERSION), blackDuckProperties);
    }

    @Override
    protected Collection<ComponentItem> getComponentItems(JsonFieldAccessor jsonFieldAccessor, List<JsonField<?>> notificationFields, AlertNotificationWrapper notificationContent) {
        ComponentItem.Builder builder = new ComponentItem.Builder();

        Long notificationId = notificationContent.getId();
        Optional<String> optionalOperationType = getStringFields(notificationFields)
                                                     .stream()
                                                     .filter(field -> BlackDuckContent.LABEL_OPERATION_TYPE.equals(field.getLabel()))
                                                     .findFirst()
                                                     .flatMap(jsonFieldAccessor::getFirst);
        if (optionalOperationType.isPresent()) {
            String operationType = optionalOperationType.get();
            ItemOperation operation = getOperation(operationType);
            LinkableItem item = createDescriptionItem(jsonFieldAccessor, notificationFields, operationType);
            builder.applyComponentData(item)
                .applyPriority(ComponentItemPriority.HIGHEST)
                .applyCategory(CATEGORY_TYPE)
                .applyOperation(operation)
                .applyNotificationId(notificationId);
            try {
                return Set.of(builder.build());
            } catch (AlertException e) {
                logger.error("Could not get component items for Project / Version notification", e);
            }
        } else {
            logger.warn("No operation type provided. Skipping this notification (id: {}, type: {}).", notificationId, notificationContent.getNotificationType());
        }
        return Set.of();
    }

    private LinkableItem createDescriptionItem(JsonFieldAccessor jsonFieldAccessor, List<JsonField<?>> notificationFields, String operationTypeString) {
        Optional<LinkableItem> subTopicItem = getSubTopicItems(jsonFieldAccessor, notificationFields)
                                                  .stream()
                                                  .findFirst();

        String projectVersionString = subTopicItem.isPresent() ? "project version" : "project";
        return new LinkableItem("Description", String.format("This %s was %sd in Black Duck.", projectVersionString, operationTypeString.toLowerCase()));
    }

    private ItemOperation getOperation(String operationType) {
        switch (operationType) {
            case "CREATE":
                return ItemOperation.ADD;
            case "DELETE":
                return ItemOperation.DELETE;
            default:
                return ItemOperation.UPDATE;
        }
    }

}
