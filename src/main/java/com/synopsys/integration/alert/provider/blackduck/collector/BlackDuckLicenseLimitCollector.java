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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.CategoryItem;
import com.synopsys.integration.alert.common.message.model.CategoryKey;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.common.workflow.MessageContentCollector;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonField;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonFieldAccessor;
import com.synopsys.integration.alert.common.workflow.processor.MessageContentProcessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderContentTypes;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BlackDuckLicenseLimitCollector extends MessageContentCollector {
    @Autowired
    public BlackDuckLicenseLimitCollector(final JsonExtractor jsonExtractor, final List<MessageContentProcessor> messageContentProcessorList) {
        super(jsonExtractor, messageContentProcessorList, Arrays.asList(BlackDuckProviderContentTypes.LICENSE_LIMIT));
    }

    @Override
    protected void addCategoryItems(final SortedSet<CategoryItem> categoryItems, final JsonFieldAccessor jsonFieldAccessor, final List<JsonField<?>> notificationFields, final AlertNotificationWrapper notificationContent) {
        final List<JsonField<Long>> longFields = getLongFields(notificationFields);

        final SortedSet<LinkableItem> linkableItems = new TreeSet<>();
        for (final JsonField<Long> field : longFields) {
            final Optional<Long> optionalValue = jsonFieldAccessor.getFirst(field);
            optionalValue.ifPresent(value -> linkableItems.add(new LinkableItem(field.getLabel(), value.toString())));
        }
        if (!linkableItems.isEmpty()) {
            linkableItems.forEach(item -> item.setSummarizable(true));
            final CategoryKey key = CategoryKey.from(notificationContent.getNotificationType(), notificationContent.getId().toString());
            categoryItems.add(new CategoryItem(key, ItemOperation.UPDATE, notificationContent.getId(), linkableItems));
        }
    }
}
