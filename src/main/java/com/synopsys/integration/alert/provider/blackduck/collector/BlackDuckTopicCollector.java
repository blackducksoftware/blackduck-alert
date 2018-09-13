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

import java.util.List;

import com.synopsys.integration.alert.common.model.TopicContent;
import com.synopsys.integration.alert.common.workflow.processor.TopicCollector;
import com.synopsys.integration.alert.common.workflow.processor.TopicFormatter;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.workflow.filter.JsonExtractor;

public abstract class BlackDuckTopicCollector extends TopicCollector {

    public BlackDuckTopicCollector(final JsonExtractor jsonExtractor, final BlackDuckProvider blackDuckProvider, final List<TopicFormatter> topicFormatterList) {
        super(jsonExtractor, blackDuckProvider, topicFormatterList);
    }

    @Override
    public void insert(final NotificationContent notification) {
        final List<TopicContent> contents = getContentsOrCreateIfDoesNotExist(notification);
        for (final TopicContent content : contents) {
            addCategoryItemsToContent(content, notification);
            addContent(content);
        }
    }

    protected abstract void addCategoryItemsToContent(final TopicContent content, final NotificationContent notification);
}
