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
package com.synopsys.integration.alert.workflow.processor.collapse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.model.CategoryItem;
import com.synopsys.integration.alert.common.model.CategoryKey;
import com.synopsys.integration.alert.common.model.LinkableItem;
import com.synopsys.integration.alert.common.model.TopicContent;

@Component
public class TopicCompressor {
    public List<TopicContent> collapseTopics(final List<TopicContent> topicList) {
        final List<TopicContent> collapsedTopicList = new ArrayList<>(topicList.size());
        topicList.forEach(topic -> {
            final Map<CategoryKey, CategoryItem> categoryDataCache = new LinkedHashMap<>();
            topic.getCategoryItemList().forEach(item -> {
                processOperation(categoryDataCache, item);
            });
            final TopicContent collapsedContent = rebuildTopic(topic, categoryDataCache.values());
            collapsedTopicList.add(collapsedContent);
        });

        return collapsedTopicList;
    }

    private void processOperation(final Map<CategoryKey, CategoryItem> categoryDataCache, final CategoryItem item) {
        final CategoryKey key = item.getCategoryKey();
        // TODO: clean up this code to perform the operation with the cache
        switch (item.getOperation()) {
            case ADD: {
                categoryDataCache.put(key, item);
                break;
            }
            case UPDATE: {
                categoryDataCache.put(key, item);
                break;
            }
            case DELETE: {
                if (categoryDataCache.containsKey(key)) {
                    categoryDataCache.remove(key);
                } else {
                    categoryDataCache.put(key, item);
                }
                break;
            }
        }
    }

    private TopicContent rebuildTopic(final TopicContent currentContent, final Collection<CategoryItem> categoryItemCollection) {
        final String url = currentContent.getUrl().orElse(null);
        final LinkableItem subTopic = currentContent.getSubTopic().orElse(null);
        return new TopicContent(currentContent.getName(), currentContent.getValue(), url, subTopic, new ArrayList<>(categoryItemCollection));
    }
}
