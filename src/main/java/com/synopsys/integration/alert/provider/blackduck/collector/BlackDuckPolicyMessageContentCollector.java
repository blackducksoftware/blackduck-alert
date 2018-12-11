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

package com.synopsys.integration.alert.provider.blackduck.collector;

import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.model.CategoryItem;
import com.synopsys.integration.alert.common.model.CategoryKey;
import com.synopsys.integration.alert.common.model.LinkableItem;
import com.synopsys.integration.alert.common.provider.ProviderContentType;
import com.synopsys.integration.alert.common.workflow.processor.MessageContentCollector;
import com.synopsys.integration.alert.common.workflow.processor.MessageContentProcessor;
import com.synopsys.integration.alert.workflow.filter.field.JsonExtractor;

public abstract class BlackDuckPolicyMessageContentCollector extends MessageContentCollector {
    public static final String CATEGORY_TYPE = "policy";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public BlackDuckPolicyMessageContentCollector(final JsonExtractor jsonExtractor, final List<MessageContentProcessor> messageContentProcessorList, final Collection<ProviderContentType> contentTypes) {
        super(jsonExtractor, messageContentProcessorList, contentTypes);
    }

    protected void addApplicableItems(final List<CategoryItem> categoryItems, final Long notificationId, final LinkableItem policyItem, final String policyUrl, final ItemOperation operation, final SortedSet<LinkableItem> applicableItems) {
        final List<String> keyItems = Stream.concat(applicableItems.stream().map(LinkableItem::getValue), Stream.of(policyUrl)).collect(Collectors.toList());
        final CategoryKey categoryKey = CategoryKey.from(CATEGORY_TYPE, keyItems);

        for (final LinkableItem item : applicableItems) {
            final SortedSet<LinkableItem> linkableItems;
            linkableItems = createLinkableItemSet(policyItem, item);
            addItem(categoryItems, new CategoryItem(categoryKey, operation, notificationId, linkableItems));
        }
    }
}
