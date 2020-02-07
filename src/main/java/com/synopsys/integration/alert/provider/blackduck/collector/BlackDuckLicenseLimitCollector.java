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
package com.synopsys.integration.alert.provider.blackduck.collector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
public class BlackDuckLicenseLimitCollector extends BlackDuckCollector {
    private final BlackDuckProperties blackDuckProperties;
    private Logger logger = LoggerFactory.getLogger(BlackDuckLicenseLimitCollector.class);

    @Autowired
    public BlackDuckLicenseLimitCollector(final JsonExtractor jsonExtractor, final BlackDuckProperties blackDuckProperties) {
        super(jsonExtractor, Arrays.asList(BlackDuckContent.LICENSE_LIMIT), blackDuckProperties);
        this.blackDuckProperties = blackDuckProperties;
    }

    @Override
    protected Collection<ComponentItem> getComponentItems(JsonFieldAccessor jsonFieldAccessor, List<JsonField<?>> notificationFields, AlertNotificationWrapper notificationContent) {
        List<ComponentItem> items = new LinkedList<>();
        final List<JsonField<Long>> longFields = getLongFields(notificationFields);

        final SortedSet<LinkableItem> linkableItems = new TreeSet<>();
        for (final JsonField<Long> field : longFields) {
            final Optional<Long> optionalValue = jsonFieldAccessor.getFirst(field);
            optionalValue.ifPresent(value -> linkableItems.add(new LinkableItem(field.getLabel(), value.toString())));
        }
        if (!linkableItems.isEmpty()) {
            linkableItems.forEach(item -> item.setSummarizable(true));

            try {
                ComponentItem.Builder builder = new ComponentItem.Builder();
                builder.applyComponentData("", "")
                    .applyAllComponentAttributes(linkableItems)
                    .applyOperation(ItemOperation.UPDATE)
                    .applyCategory(notificationContent.getNotificationType())
                    .applyNotificationId(notificationContent.getId());
                items.add(builder.build());
            } catch (AlertException ex) {
                logger.error("Error building license limit component item ", ex);
            }
        }

        return items;
    }

    @Override
    protected List<LinkableItem> getTopicItems(final JsonFieldAccessor accessor, final List<JsonField<?>> fields) {
        final List<LinkableItem> topicItems = super.getTopicItems(accessor, fields);
        final String blackDuckUrl = blackDuckProperties.getBlackDuckUrl().orElse(null);

        final List<LinkableItem> newTopicItems = new ArrayList<>();
        for (final LinkableItem item : topicItems) {
            final Optional<String> optionalUrl = item.getUrl();
            if (optionalUrl.isEmpty()) {
                newTopicItems.add(new LinkableItem(item.getName(), item.getValue(), blackDuckUrl));
            } else {
                newTopicItems.add(item);
            }
        }
        return newTopicItems;
    }

}
